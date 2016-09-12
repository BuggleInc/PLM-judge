package main.java.server.listener;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;

import com.rabbitmq.client.Channel;

import main.java.server.Connector;
import plm.core.model.json.JSONUtils;
import plm.universe.World;

/**
 * The {@link IWorldView} implementation. Linked to the current {@link Game} instance, and is called every time the world moves. 
 * <p>
 * It does two things : first, it creates update chunks via {@link StreamMsg} to describe the world movement.
 * It also aggregates these messages to send them to the given {@link Channel} at least past the given time delay.
 * @author Tanguy
 * @see StreamMsg
 */
public class BasicListener {

	private static final int MAX_SIZE = 10000;

	private World currWorld = null;
	Connector connector;
	long lastTime = System.currentTimeMillis();
	long delay;

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
				if(!currWorld.getSteps().isEmpty()) {
					sendOperations(currWorld, MAX_SIZE);
				}
			}
		};

		ses.scheduleAtFixedRate(cmd, 0, 1000, TimeUnit.MILLISECONDS);
	}

	public void sendOperations(World currWorld, int nbMessages) {
		try {
			send(JSONUtils.operationsToJSON(currWorld, nbMessages));
		} catch (OutOfMemoryError e) {
			// We want to stop the JVM to be able to restart the judge
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Set or replaces the game world to listen to.
	 * @param world
	 */
	public void setWorld(World world) {
		currWorld = world;
	}

	public void flush() {
		while(!currWorld.getSteps().isEmpty()) {
			sendOperations(currWorld, MAX_SIZE);
	  	}
	}

	@Override
	public BasicListener clone() {
		return new BasicListener(connector, delay);
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
	}
	
	/**
	 * Sends all accumulated messages.
	 */
	public void send(String message) {
		Channel channel = connector.cOut();
		String sendTo = connector.cOutName();

		try {
			channel.basicPublish("", sendTo, null, message.getBytes("UTF-8"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void stopSes() {
		ses.shutdown();
		try {
			ses.awaitTermination(1L, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IllegalMonitorStateException e) {
			// We want to stop the JVM to be able to restart the judge
			e.printStackTrace();
			System.exit(0);
		}
	}
}
