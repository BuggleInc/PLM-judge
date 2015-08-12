package generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.Semaphore;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.rabbitmq.client.AMQP.BasicProperties;

import plm.core.model.Game;
import plm.core.model.LogHandler;
import plm.core.model.lesson.Exercise;
import plm.core.model.lesson.Exercise.WorldKind;
import plm.core.model.lesson.Lecture;
import plm.core.model.lesson.Lesson;
import plm.core.utils.FileUtils;
import plm.universe.World;
import server.Connector;

public class GameGest {
	private Game game;
	private String exerciseID;
	private BasicListener listener;
	private ResultListener resultLstn;
	private ListenerOutStream listenerOut;
	public Semaphore endExercise = new Semaphore(0);
	private ArrayList<BasicListener> lCumul = new ArrayList<BasicListener>();
	
	public GameGest(Connector connector, LogHandler logger) {
		game = new Game("test", logger, Locale.FRENCH,"Java" , false);
		listener = new BasicListener(connector, 500);
		resultLstn = new ResultListener(connector, this);
		listenerOut = new ListenerOutStream(System.out, listener);
		PrintStream outStream = new PrintStream(listenerOut, true);  //Direct to MyOutputStream, autoflush
        System.setOut(outStream); 
	}
	
	public void setGameState(Locale locale, String language, String lessonID, String exerciseID) {
		try {
			game.setLocale(locale);
		}
		catch (Exception e) {
			System.err.println(" [E] Error while setting Locale.");
			e.printStackTrace();
		}
		try {
			game.setProgrammingLanguage(language);
		}
		catch (Exception e) {
			System.err.println(" [E] Error while setting Prog. Language.");
			e.printStackTrace();
		}
		game.switchLesson(lessonID, false);
		game.switchExercise(exerciseID);
		this.exerciseID = exerciseID;
		lCumul = new ArrayList<BasicListener>();
		// Bind listener to game
		listener.setWorld(game.getSelectedWorld());
		for(World w : ((Exercise) game.getCurrentLesson().getCurrentExercise()).getWorlds(WorldKind.ANSWER)) {
			BasicListener l = listener.clone();
			l.setWorld(w);
			lCumul.add(l);
		}
		resultLstn.setGame(game);
	}
	
	public void setCode(String code) {
	    ((Exercise) game.getCurrentLesson().getCurrentExercise()).getSourceFile(game.getProgrammingLanguage(), 0).setBody(code);
	}
	
	public void startGame() {
		Generator.generateData(exerciseID, ((Exercise) game.getCurrentLesson().getCurrentExercise()).getWorlds(WorldKind.INITIAL));
		game.startExerciseDemoExecution();
		try {
			endExercise.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void setProperties(BasicProperties properties) {
		listener.setProps(properties);
		for(BasicListener l : lCumul)
			l.setProps(properties);
		resultLstn.setProps(properties);
	}
	
	@SuppressWarnings("unchecked")
	public void sendStream() {
		JSONArray accu = new JSONArray();
		accu.addAll(listener.send());
		for(BasicListener l : lCumul)
			accu.addAll(l.send());
		try {
	        BufferedWriter bw = new BufferedWriter(new FileWriter(Generator.demoPath + exerciseID + ".json"));
	        bw.write("[");
	        if(accu.size() > 0)
	        	bw.write(((JSONObject) accu.get(0)).toJSONString());
	        for(int i = 1; i < accu.size(); i++) {
	        	Object obj = accu.get(i);
	        	bw.write(",");
	        	bw.write(((JSONObject) obj).toJSONString());
	        }
	        bw.write("]");
	        bw.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void free() {
		endExercise.release();
	}
	
	public void stop() {
		game.quit();
	}

	public Collection<String> getLessons() {
		Collection<String> res = new ArrayList<String>();
		Collection<Lesson> lessons = game.getLessons();
		Generator.generateData(lessons);
		for(Lesson l : lessons) {
			res.add("lessons." + l.getId());
		}
		return res;
	}
	
	public Collection<String> getExercises(String lesson) {
		game.switchLesson(lesson, false);
		Collection<Lecture> exercises = game.getCurrentLesson().getRootLectures();
		Generator.generateData(lesson, exercises);
		return massConvert(exercises);
	}
	
	private Collection<String> massConvert(Collection<Lecture> lects) {
		ArrayList<String> res = new ArrayList<String>();
		for(Lecture l : lects) {
			res.add(l.getId());
			res.addAll(massConvert(l.getDependingLectures()));
		}
		return res;
	}
}
