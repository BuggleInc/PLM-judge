package main.java.server;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

import main.java.server.listener.BasicListener;
import main.java.server.parser.RequestMsg;
import plm.core.lang.ProgrammingLanguage;
import plm.core.log.Logger;
import plm.core.model.json.JSONUtils;
import plm.core.model.lesson.ExecutionProgress;
import plm.core.model.lesson.ExecutionProgress.outcomeKind;
import plm.core.model.lesson.Exercise;
import plm.core.model.lesson.Exercise.WorldKind;
import plm.core.model.lesson.ExerciseRunner;
import plm.core.model.lesson.UserSettings;
import plm.universe.World;

public class Judge {

	private Connector connector;

	private ExerciseRunner exerciseRunner;
	private List<BasicListener> listeners = new ArrayList<BasicListener>();

	public Judge(Connector connector) {
		this.connector = connector;
	}

	public void handleMessage() {
		Logger.log(0, "Retrieving request handler.");
		connector.prepDelivery();

		Logger.log(0, "Waiting for request");
		QueueingConsumer.Delivery delivery = connector.getDelivery();
		String message = "";
		try {
			message = new String(delivery.getBody(),"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		RequestMsg request = new RequestMsg(message);

		exerciseRunner = new ExerciseRunner(request.getLocalization());
		exerciseRunner.setMaxNumberOfTries(5);

		setReplyQueue(request.getReplyQueue());

		Exercise exo = request.getExercise();
		setListeners(exo);
		
		ProgrammingLanguage progLang = ProgrammingLanguage.getProgrammingLanguage(request.getLanguage());
		String code = request.getCode();

		exo.setSettings(new UserSettings(request.getLocalization(), progLang));

		ExecutionProgress result = null;
		try {
			result = exerciseRunner.run(exo, progLang, code).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		stopListeners();
		if(result.outcome != outcomeKind.TIMEOUT) {
			flushListeners();
		}
		sendResult(result);
	}

	public void setReplyQueue(String replyQueue) {
		connector.initReplyQueue(replyQueue);
		Logger.log(0, "Received request from '" + replyQueue + "'.");
		sendAck();
		Logger.log(0, "Send ack");
	}

	public void setListeners(Exercise exo) {
		ListenerOutStream listenerOut = null;
		for(World w : exo.getWorlds(WorldKind.CURRENT)) {
			BasicListener l = new BasicListener(connector, 1000);
			listeners.add(l);
			if(listenerOut == null) {
				listenerOut = new ListenerOutStream(System.out, l);
				PrintStream outStream = new PrintStream(listenerOut, true);  //Direct to MyOutputStream, autoflush
		        System.setOut(outStream);
			}
			l.setWorld(w);
		}
	}

	public void stopListeners() {
		for(BasicListener l : listeners) {
			l.stopSes();
		}
	}

	public void flushListeners() {
		for(BasicListener l : listeners) {
			l.flush();
		}
	}

	public void sendAck() {
		Channel channel = connector.cOut();
		Map<String, Object> mapArgs = new HashMap<String, Object>();

		String message = JSONUtils.createMessage("ack", mapArgs);
		String sendTo = connector.cOutName();
		try {
			channel.basicPublish("", sendTo, null, message.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendResult(ExecutionProgress result) {
		Channel channel = connector.cOut();

		Map<String, Object> mapArgs = new HashMap<String, Object>();
		mapArgs.put("result", result);

		String message = JSONUtils.createMessage("executionResult", mapArgs);
		String sendTo = connector.cOutName();
		try {
			channel.basicPublish("", sendTo, null, message.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
