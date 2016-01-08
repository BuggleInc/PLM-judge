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
	JSONArray accu = new JSONArray();
	
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
	@SuppressWarnings("unchecked")
	public void worldHasMoved() {
		Long currentTime = System.currentTimeMillis();
		int length = currWorld.getSteps().size();
		for(int i=cnt; i<length; i++) {
			List<Operation> operations = currWorld.getSteps().get(i);
			
			JSONObject mapArgs = new JSONObject();
			
			JSONArray jsonOperations = new JSONArray();
			for(Operation operation: operations) {
				jsonOperations.add(operation.toJSON());
			}
			
			mapArgs.put("operations", jsonOperations);
			mapArgs.put("worldID", currWorld.getName());
			
			accu.add(mapArgs);
			if(lastTime + delay <= currentTime) {
				System.err.println("On envoie");
				lastTime = currentTime;
				send();
			}
			else {
				System.err.println("On bufferize");
			}
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
		accu.add(msgItem);
	}
	
	/**
	 * Sends all accumulated messages.
	 */
	@SuppressWarnings("unchecked")
	public void send() {
		if(!accu.isEmpty()) {
			Channel channel = connector.cOut();
			String sendTo = connector.cOutName();
			lastTime = System.currentTimeMillis();
			JSONObject bufferJson = new JSONObject();
			bufferJson.put("buffer", accu);
			
			JSONObject mapArgs = new JSONObject();
			mapArgs.put("args", bufferJson);
			
			String message = mapArgs.toJSONString();

			// Hack to start with {"cmd":"operations", ... }
			message = "{\"cmd\":\"operations\"," + message.substring(1);

			try {
				channel.basicPublish("", sendTo, null, message.getBytes("UTF-8"));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			Main.logger.log(0, "Sent stream message (" + sendTo + ")");
			accu.clear();
		}
	}
}
