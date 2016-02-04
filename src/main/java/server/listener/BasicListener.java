package main.java.server.listener;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.rabbitmq.client.Channel;

import plm.core.log.Logger;
import plm.core.model.Game;
import plm.universe.IWorldView;
import plm.universe.Operation;
import plm.universe.World;
import main.java.server.Connector;
import main.java.server.parser.StreamMsg;

/**
 * The {@link IWorldView} implementation. Linked to the current {@link Game} instance, and is called every time the world moves. 
 * <p>
 * It does two things : first, it creates update chunks via {@link StreamMsg} to describe the world movement.
 * It also aggregates these messages to send them to the given {@link Channel} at least past the given time delay.
 * @author Tanguy
 * @see StreamMsg
 */
public class BasicListener implements IWorldView {

	private static final int MAX_SIZE = 10000;

	private World currWorld = null;
	Connector connector;
	long lastTime = System.currentTimeMillis();
	long delay;
	JSONArray buffer = new JSONArray();

	private ScheduledExecutorService ses;

	/**
	 * The {@link BasicListener} constructor.
	 * @param connector the used connector
	 * @param timeout The minimal time between two stream messages. Set to 0 to stream each operation individually.
	 */
	public BasicListener(Connector connector, long delay) {
		this.delay = delay;
		this.connector = connector;
		
		ses = Executors.newSingleThreadScheduledExecutor();
		
		Runnable cmd = new Runnable() {
			@Override
			public void run() {
				int length = currWorld.getSteps().size();
				if(MAX_SIZE < length) {
					length = MAX_SIZE;
				}
				for(int i=0; i<length; i++) {
					List<Operation> operations = currWorld.getSteps().poll();
					Operation.addOperationsToBuffer(buffer, currWorld.getName(), operations);
				}
				send();
			}
		};

		ses.scheduleAtFixedRate(cmd, 0, 1000, TimeUnit.MILLISECONDS);
	}

	/**
	 * Set or replaces the game world to listen to.
	 * @param world
	 */
	public void setWorld(World world) {
		currWorld = world;
		currWorld.addWorldUpdatesListener(this);
	}

	public void flush() {
		int length = currWorld.getSteps().size();
		for(int i=0; i<length; i++) {
			List<Operation> operations = currWorld.getSteps().poll();
			Operation.addOperationsToBuffer(buffer, currWorld.getName(), operations);
			if(buffer.size()==MAX_SIZE) {
				send();
			}
		}
		send();
	}

	@Override
	public BasicListener clone() {
		return new BasicListener(connector, delay);
	}
	
	@Override
	public void worldHasMoved() {
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
			String message = Operation.operationsBufferToMsg("operations", buffer);

			try {
				channel.basicPublish("", sendTo, null, message.getBytes("UTF-8"));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			Logger.log(2, "Sent stream message (" + sendTo + ")");
			buffer.clear();
		}
	}

	public void stopSes() {
	ses.shutdown();
		try {
			ses.awaitTermination(1L, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
