package server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;

import plm.core.model.Game;
import plm.core.model.LogHandler;
import plm.core.model.lesson.Exercise;
import server.listener.BasicListener;
import server.listener.ResultListener;
import server.parser.*;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class Main {
	private final static String QUEUE_NAME_REQUEST = "worker_in";
	private final static String QUEUE_NAME_REPLY = "worker_out";
	private final static LogHandler logger = new ServerLogHandler();
	private static Game game = null;
	public static Semaphore endExercise = new Semaphore(0);
	private static String host;
	private static String port;
	private static BasicListener listener;
	private static ResultListener resultLstn;
	
	private static Channel channelIn;
	private static Channel channelOut;
	
	public static void askEndStreamMain() {
		listener.send();
	}
	
	public static void freeMain() {
		endExercise.release();
	}

	public static void initData() {
		System.out.println("Started Worker on queue server at : " + host);
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		factory.setPort(Integer.parseInt(port));
		Connection connection;
		try {
			connection = factory.newConnection();
			channelIn = connection.createChannel();
			channelOut = connection.createChannel();
			channelIn.queueDeclare(QUEUE_NAME_REQUEST, false, false, false, null);
			channelOut.queueDeclare(QUEUE_NAME_REPLY, false, false, false, null);
			
			// Create game.
			System.out.println(" [D] Creating game.");
			game = new Game("test", logger, Locale.FRENCH,"Java" , false);
			listener = new BasicListener(channelOut,  QUEUE_NAME_REPLY);
			resultLstn = new ResultListener(channelOut, QUEUE_NAME_REPLY);
		} catch (IOException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
	}
	
	public static void mainLoop() {

		System.out.println(" [D] Waiting for request.");
		QueueingConsumer consumer = new QueueingConsumer(channelIn);
		try {
			channelIn.basicConsume(QUEUE_NAME_REQUEST, true, consumer);
		}
		catch (IOException e) {
			e.printStackTrace();
			
		}
		while (true) {
			QueueingConsumer.Delivery delivery = null;
			try {
				delivery = consumer.nextDelivery();
			} catch (ShutdownSignalException | ConsumerCancelledException
					| InterruptedException e2) {
				e2.printStackTrace();
				return;
			}
			BasicProperties props = delivery.getProperties();
		    BasicProperties replyProps = new BasicProperties
                    .Builder()
                    .correlationId(props.getCorrelationId())
                    .build();
			String message = "";
			try {
				message = new String(delivery.getBody(),"UTF-8");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			System.out.println(" [D] Received request from '" + props.getCorrelationId() + "'.");
			RequestMsg request = RequestMsg.readMessage(message);
			// Setting return data
			listener.setProps(replyProps);
			resultLstn.setProps(replyProps);
			// Set game state
			try {
				Locale lang = Locale.forLanguageTag(request.getLocale());
				game.setLocale(lang);
			}
			catch (Exception e) {
				System.err.println(" [E] Error while setting Locale. (original message : '" + message + "'");
				e.printStackTrace();
			}
			try {
				game.setProgrammingLanguage(request.getLanguage());
			}
			catch (Exception e) {
				System.err.println(" [E] Error while setting Prog. Language. (original message : '" + message + "'");
				e.printStackTrace();
			}
			game.switchLesson(request.getLessonID(), false);
			game.switchExercise(request.getExerciseID());
			// Bind listener to game
			listener.setWorld(game.getSelectedWorld());
			resultLstn.setGame(game);
			// Put code in compiler.
			System.out.println(" [D] Starting compilation.");
		    ((Exercise) game.getCurrentLesson().getCurrentExercise()).getSourceFile(game.getProgrammingLanguage(), 0).setBody(request.getCode());
			// Start the game.
			game.startExerciseExecution();
			// Delete the game instance.
			try {
				if(!endExercise.tryAcquire(30, java.util.concurrent.TimeUnit.SECONDS)) {
					game.stopExerciseExecution();
				}
					
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(" [D] Ended compilation.");
		}
	}

	public static void main(String[] argv) {
		host = System.getenv("MESSAGEQ_PORT_5672_TCP_ADDR") != null
				? System.getenv("MESSAGEQ_PORT_5672_TCP_ADDR")
				: "localhost";
		port = System.getenv("MESSAGEQ_PORT_5672_TCP_PORT") != null
				? System.getenv("MESSAGEQ_PORT_5672_TCP_PORT")
				: "5672";
		initData();
		mainLoop();
	}
}
