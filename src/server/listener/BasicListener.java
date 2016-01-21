package server.listener;

import java.io.IOException;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import plm.core.model.Game;
import plm.universe.IWorldView;
import plm.universe.Operation;
import plm.universe.World;
import server.Connector;
import server.Main;
import server.parser.StreamMsg;

import com.rabbitmq.client.Channel;

/**
 * The {@link IWorldView} implementation. Linked to the current {@link Game} instance, and is called every time the world moves. 
 * <p>
 * It does two things : first, it creates update chunks via {@link StreamMsg} to describe the world movement.
 * It also aggregates these messages to send them to the given {@link Channel} at least past the given time delay.
 * @author Tanguy
 * @see StreamMsg
 */
public class BasicListener implements IWorldView {

	private World currWorld = null;
	Connector connector;
	long lastTime = System.currentTimeMillis();
	long delay;
	JSONArray buffer = new JSONArray();
	
	int cnt = 0;

	/**
	 * The {@link BasicListener} constructor.
	 * @param connector the used connector
	 * @param timeout The minimal time between two stream messages. Set to 0 to stream each operation individually.
	 */
	public BasicListener(Connector connector, long delay) {
		this.delay = delay;
		this.connector = connector;
	}

	/**
	 * Set or replaces the game world to listen to.
	 * @param world
	 */
	public void setWorld(World world) {
		currWorld = world;
		currWorld.addWorldUpdatesListener(this);
	}
	
	@Override
	public BasicListener clone() {
		return new BasicListener(connector, delay);
	}
	
	@Override
	public void worldHasMoved() {
		Long currentTime = System.currentTimeMillis();
		int length = currWorld.getSteps().size();
		for(int i=cnt; i<length; i++) {
			List<Operation> operations = currWorld.getSteps().get(i);
			
			Operation.addOperationsToBuffer(buffer, currWorld.getName(), operations);
		}
		if(lastTime + delay <= currentTime) {
			System.err.println("On envoie");
			lastTime = System.currentTimeMillis();
			send();
		}
		else {
			System.err.println("On bufferize");
		}
		cnt = length;
	}

	@Override
	public void worldHasChanged() {
		// NO OP
	}

	/**
	 * 
	 * @param msg the message to send as MessageStream
	 */
	@SuppressWarnings("unchecked")
	public void streamOut(String msg) {
		JSONObject res = new JSONObject();
		res.put("cmd", "outputStream");
		res.put("msg", msg);
		addOperations(res);
	}
	
	@SuppressWarnings("unchecked")
	private void addOperations(JSONObject msgItem) {
		buffer.add(msgItem);
	}
	
	/**
	 * Sends all accumulated messages.
	 */
	public void send() {
		if(!buffer.isEmpty()) {
			Channel channel = connector.cOut();
			String sendTo = connector.cOutName();

			// Hack to start with {"cmd":"operations", ... }
			String message = Operation.operationsBufferToMsg(buffer);

			try {
				channel.basicPublish("", sendTo, null, message.getBytes("UTF-8"));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			Main.logger.log(0, "Sent stream message (" + sendTo + ")");
			buffer.clear();
		}
	}
}
