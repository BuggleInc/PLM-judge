package server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class Connector {
	private final static String QUEUE_NAME_REQUEST = "worker_in";
	
	private String replyQueueName = "";
	private Connection connection;
	protected Channel channelIn;
	protected Channel channelOut;
	
    private Long defaultTimeout = new Long(10000);

	private QueueingConsumer consumer;
	
	public Connector(String host, int port) {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		factory.setPort(port);
		try {
			connection = factory.newConnection();
			channelIn = connection.createChannel();
            Map<String, Object> args = new HashMap<String, Object>();
            args.put("x-message-ttl", defaultTimeout);
			channelIn.queueDeclare(QUEUE_NAME_REQUEST, false, false, false, args);
		} catch (IOException e) {
			Main.logger.log(2, "Host unknown. Aborting...");
			System.exit(1);
	    } catch (TimeoutException e) {
	    	Main.logger.log(2, "Host timed out. Aborting...");
			System.exit(1);
		}
	}
	
	public void initReplyQueue(String replyQueueName) {
		this.replyQueueName = replyQueueName;
		try {
			channelOut = connection.createChannel();
            Map<String, Object> args = new HashMap<String, Object>();
            args.put("x-message-ttl", 5000);
			channelOut.queueDeclare(replyQueueName, false, false, true, args);
		} catch (IOException e) {
			Main.logger.log(2, "Host unknown. Aborting...");
			System.exit(1);
	    }
	}
	
	public void closeConnections() {
		try {
			channelIn.close();
			channelOut.close();
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	}
	
	public void prepDelivery() {
		consumer = new QueueingConsumer(channelIn);
		try {
			channelIn.basicConsume(QUEUE_NAME_REQUEST, true, consumer);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public QueueingConsumer.Delivery getDelivery() {
		QueueingConsumer.Delivery delivery = null;
		try {
			delivery = consumer.nextDelivery();
		} catch (ShutdownSignalException | ConsumerCancelledException
				| InterruptedException e2) {
			e2.printStackTrace();
		}
		return delivery;
	}
	
	public Channel cOut() {
		return channelOut;
	}
	
	public String cOutName() {
		return replyQueueName;
	}

}
