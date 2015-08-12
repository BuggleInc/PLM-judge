package server;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.*;

public class Connector {
	private final static String QUEUE_NAME_REQUEST = "worker_in";
	private final static String QUEUE_NAME_REPLY = "worker_out";
	
	protected Channel channelIn;
	protected Channel channelOut;
	
	private QueueingConsumer consumer;
	
	public void init(String host, int port) {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		factory.setPort(port);
		Connection connection;
		try {
			connection = factory.newConnection();
			channelIn = connection.createChannel();
			channelOut = connection.createChannel();
			channelIn.queueDeclare(QUEUE_NAME_REQUEST, false, false, false, null);
			channelOut.queueDeclare(QUEUE_NAME_REPLY, false, false, false, null);
		} catch (IOException e) {
			Main.logger.log(2, "Host unknown. Aborting...");
			System.exit(1);
	    } catch (TimeoutException e) {
	    	Main.logger.log(2, "Host timed out. Aborting...");
			System.exit(1);
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
		return QUEUE_NAME_REPLY;
	}

}
