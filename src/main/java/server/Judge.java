package main.java.server;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import plm.core.lang.ProgrammingLanguage;
import plm.core.log.Logger;
import plm.core.model.lesson.ExecutionProgress;
import plm.core.model.lesson.ExecutionProgress.outcomeKind;
import plm.core.model.lesson.Exercise;
import plm.core.model.lesson.Exercise.WorldKind;
import plm.core.model.lesson.ExerciseFactory;
import plm.core.model.lesson.ExerciseRunner;
import plm.universe.World;
import main.java.server.listener.BasicListener;
import main.java.server.parser.RequestMsg;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class Judge {
	
	private Connector connector;
	private I18n i18n;

	private ExerciseRunner exerciseRunner;
	private List<BasicListener> listeners = new ArrayList<BasicListener>();
	
	public Judge(Connector connector) {
		this.connector = connector;
		
		i18n = I18nFactory.getI18n(getClass(), "org.plm.i18n.Messages", new Locale("en"), I18nFactory.FALLBACK);
		exerciseRunner = new ExerciseRunner(i18n);
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
		
		RequestMsg request = RequestMsg.readMessage(message);

		setReplyQueue(request.getReplyQueue());
		
		Exercise exo = getExercise(request.getJSONExercise());
		setListeners(exo);
		
		ProgrammingLanguage progLang = ProgrammingLanguage.getProgrammingLanguage(request.getLanguage());
		String code = request.getCode();
		
		ExecutionProgress result = null;
		try {
			result = exerciseRunner.run(exo, progLang, code).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stopListeners();
		if(result.outcome != outcomeKind.TIMEOUT) {
			flushListeners();
		}
		sendResult(result);
		System.err.println("Result: " + result.toJSON().toString());
	}
	
	public void setReplyQueue(String replyQueue) {
		connector.initReplyQueue(replyQueue);
		Logger.log(0, "Received request from '" + replyQueue + "'.");
		sendAck();
		Logger.log(0, "Send ack");
	}
	
	public Exercise getExercise(String jsonExercise) {
		System.err.println("jsonExercise: " + jsonExercise);
		JSONObject obj = (JSONObject) JSONValue.parse(jsonExercise);
		return ExerciseFactory.exerciseFromJson(obj);
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
			w.setDelay(0);
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

	@SuppressWarnings("unchecked")
	public void sendAck() {
		Channel channel = connector.cOut();
		JSONObject msgJson = new JSONObject();
		msgJson.put("cmd", "ack");
		String message = msgJson.toJSONString();
		String sendTo = connector.cOutName();
		try {
			channel.basicPublish("", sendTo, null, message.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void sendResult(ExecutionProgress result) {
		Channel channel = connector.cOut();
		JSONObject msgJson = new JSONObject();
		msgJson.put("cmd", "executionResult");
		msgJson.put("result", result.toJSON());
		String message = msgJson.toJSONString();
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
