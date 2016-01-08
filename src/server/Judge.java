package server;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import plm.core.lang.ProgrammingLanguage;
import plm.core.model.LogHandler;
import plm.core.model.lesson.ExecutionProgress;
import plm.core.model.lesson.Exercise;
import plm.core.model.lesson.Exercise.WorldKind;
import plm.core.model.lesson.ExerciseFactory;
import plm.core.model.lesson.ExerciseRunner;
import plm.universe.World;
import server.listener.BasicListener;
import server.parser.RequestMsg;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class Judge {
	
	private Connector connector;
	private LogHandler logger;
	private I18n i18n;

	private ExerciseRunner exerciseRunner;
	private List<BasicListener> listeners = new ArrayList<BasicListener>();
	
	public Judge(Connector connector) {
		this.connector = connector;
		
		logger = new ServerLogHandler();
		i18n = I18nFactory.getI18n(getClass(), "org.plm.i18n.Messages", new Locale("en"), I18nFactory.FALLBACK);
		exerciseRunner = new ExerciseRunner(logger, i18n);
	}
	
	public void handleMessage() {
		logger.log(0, "Retrieving request handler.");
		connector.prepDelivery();
		
		logger.log(0, "Waiting for request");
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
		
		ExecutionProgress result = exerciseRunner.run(exo, progLang, code);
		flushListeners();
		sendResult(result);
		System.err.println("Result: " + result.toJSON().toString());
	}
	
	public void setReplyQueue(String replyQueue) {
		connector.initReplyQueue(replyQueue);
		logger.log(0, "Received request from '" + replyQueue + "'.");
		sendAck();
		logger.log(0, "Send ack");
	}
	
	public Exercise getExercise(String jsonExercise) {
		System.err.println("jsonExercise: " + jsonExercise);
		JSONObject obj = (JSONObject) JSONValue.parse(jsonExercise);
		return ExerciseFactory.exerciseFromJson(obj);
	}
	
	public void setListeners(Exercise exo) {
		BasicListener listener = new BasicListener(connector, 1000);
		ListenerOutStream listenerOut = null;
		for(World w : exo.getWorlds(WorldKind.CURRENT)) {
			BasicListener l = listener.clone();
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

	public void flushListeners() {
		for(BasicListener l : listeners) {
			l.send();
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
