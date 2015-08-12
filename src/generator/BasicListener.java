package generator;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;

import plm.core.model.Game;
import plm.universe.Entity;
import plm.universe.IWorldView;
import plm.universe.World;
import server.Connector;
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
	 * @param connector the used connector
	 * @param timeout The minimal time between two stream messages. Set to 0 to stream each operation individually.
	 */
	public BasicListener(Connector connector, long timeout) {
		this.timeout = timeout;
		this.channel = connector.cOut();
		this.sendTo = connector.cOutName();
	}
	
	private BasicListener(Channel c, String sTo, long t) {
		this.timeout = t;
		this.channel = c;
		this.sendTo = sTo;
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
		BasicListener copy = new BasicListener(channel, sendTo, timeout);
		copy.setProps(properties);
		copy.setWorld(currWorld);
		return copy;
	}
	
	@Override
	public void worldHasMoved() {
		List<Entity> l = currWorld.getEntities();
		for(Entity element : l) {
			if(element.isReadyToSend()) {
				StreamMsg streamMsg = new StreamMsg(currWorld, element.getOperations());
				element.getOperations().clear();
				element.setReadyToSend(false);
				JSONObject message = streamMsg.result();
				send(message);
			}
		}
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
		res.put("type", "outputStream");
		res.put("msg", msg);
		send(res);
	}
	
	/**
	 * Sends the given message, or accumulates it if the timeout isn't reached. This method is private.
	 * @param msgItem the JSON message to be send.
	 * @see StreamMsg
	 */
	@SuppressWarnings("unchecked")
	private void send(JSONObject msgItem) {
		accu.add(msgItem);
	}
	
	/**
	 * Sends all accumulated messages.
	 */
	@SuppressWarnings("unchecked")
	public JSONArray send() {
		return accu;
	}
}
