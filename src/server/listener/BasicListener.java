package server.listener;

import java.io.IOException;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;

import plm.core.model.Game;
import plm.universe.Entity;
import plm.universe.IWorldView;
import plm.universe.World;
import server.parser.StreamMsg;

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
	Channel channel;
	String sendTo;
	BasicProperties properties;
	long execTime;
	long timeout;
	JSONArray accu;
	
	/**
	 * The {@link BasicListener} constructor.
	 * @param channel Channel the basicListener shoud push to.
	 * @param sendTo The channel name. It should be the same that the one used while creating channel
	 * @param timeout The minimal time between two stream messages. Set to 0 to stream each operation individually.
	 */
	public BasicListener(Channel channel, String sendTo, long timeout) {
		this.timeout = timeout;
		this.channel = channel;
		this.sendTo = sendTo;
	}
	
	/**
	 * Set the reply properties value. Also, reset the accumulation queue.
	 * @param properties The properties sent with each message
	 */
	public void setProps(BasicProperties properties) {
		this.properties = properties;
		accu = new JSONArray();
	}
	
	/**
	 * Set or replaces the game world to listen to.
	 * @param world
	 */
	public void setWorld(World world) {
		if(currWorld != null)
			currWorld.removeWorldUpdatesListener(this);
		currWorld = world;
		currWorld.addWorldUpdatesListener(this);
	}
	
	@Override
	public BasicListener clone() {
		BasicListener res = new BasicListener(channel, sendTo, timeout);
		res.setWorld(currWorld);
		return res;
	}
	
	@Override
	public void worldHasMoved() {
		List<Entity> l = currWorld.getEntities();
		for(Entity element : l) {
			if(element.isReadyToSend()) {
				StreamMsg streamMsg = new StreamMsg(currWorld, element.getOperations());
				JSONObject message = streamMsg.result();
				element.getOperations().clear();
				element.setReadyToSend(false);
				send(message);
			}
		}
	}

	@Override
	public void worldHasChanged() {
		// TODO explain why it's empty.
	}

	/**
	 * Sends the given message, or accumulates it if the timeout isn't reached. This method is private.
	 * @param msgItem the JSON message to be send.
	 * @see StreamMsg
	 */
	@SuppressWarnings("unchecked")
	private void send(JSONObject msgItem) {
		long timer = System.currentTimeMillis() - this.execTime;
		accu.add(msgItem);
		if(timer > timeout) {
			this.execTime = System.currentTimeMillis();
			send();
		}
	}
	
	/**
	 * Sends all accumulated messages.
	 */
	@SuppressWarnings("unchecked")
	public void send() {
		JSONObject msgJson = new JSONObject();
		msgJson.put("type", "stream");
		msgJson.put("content", accu);
		String message = accu.toJSONString();
		try {
			channel.basicPublish("", sendTo, properties, message.getBytes("UTF-8"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.out.println(" [D] Sent stream message (" + properties.getCorrelationId() + ")");
	}
}
