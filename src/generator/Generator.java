package generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.apache.commons.cli.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.rabbitmq.client.AMQP.BasicProperties;

import generator.parser.WorldParser;
import plm.core.model.lesson.Lecture;
import plm.core.model.lesson.Lesson;
import plm.universe.World;
import server.Main;

public class Generator {
	private GameGest gest = null;
	private ConnectorDummy connector = new ConnectorDummy();
	private static int testID = 1;
	public static String demoPath = "lessonDemos";
	public static String worldPath = "lessonWorlds";
	public static String dataPath = "webPlmData";
	
	public Generator init() {
		connector.init("kappa", 1000);
		gest = new GameGest(connector, Main.logger);
		BasicProperties replyProps = new BasicProperties
                .Builder()
                .correlationId("TestCorrID")
                .build();
		gest.setProperties(replyProps);
		return this;
	}
	
	public int run() {
		Collection<String> cLess = gest.getLessons();
		for(String lessID : cLess) {
			Collection<String> cExo = gest.getExercises(lessID);
			for(String exID : cExo) {
				init();
				logInfos("Generation "+ (testID++), lessID, exID);
				gest.setGameState(Locale.FRENCH, "Java", lessID, exID);
				gest.startGame();
			}
		}
		return 1;
	}

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		CommandLine commandLine;
        Option option_wp = OptionBuilder.withArgName("wp").hasArg().withDescription("The world path").create("w");
        Option option_op = OptionBuilder.withArgName("op").hasArg().withDescription("The demo operation path").create("o");
        Option option_dp = OptionBuilder.withArgName("dp").hasArg().withDescription("The data path").create("d");
        Options options = new Options().addOption(option_wp).addOption(option_op).addOption(option_dp);
		Main.logger.log(-1, "Starting generator.");
		Generator g = new Generator().init();
        try {
        	commandLine = (new GnuParser()).parse(options, args);
        	if(commandLine.hasOption("w"))
        		worldPath = commandLine.getOptionValue("w");
        	if(commandLine.hasOption("o"))
        		demoPath = commandLine.getOptionValue("o");
        	if(commandLine.hasOption("d"))
        		dataPath = commandLine.getOptionValue("d");
        }
        catch(ParseException e) {
        	e.printStackTrace();
        }
		checkFolders();
        g.run();
	}
	
	public static void checkFolders() {
		File worldFolder = new File(worldPath);
		File demoFolder = new File(demoPath);
		File dataFolder = new File(dataPath);
		try {
			if(!worldFolder.exists() || !worldFolder.isDirectory())
				worldFolder.mkdir();
			if(!demoFolder.exists() || !demoFolder.isDirectory())
				demoFolder.mkdir();
			if(!dataFolder.exists() || !dataFolder.isDirectory())
				dataFolder.mkdir();
		}
		catch(SecurityException e) {
			e.printStackTrace();
		}
		if(!worldPath.endsWith("//"))
			worldPath += "//";
		if(!demoPath.endsWith("//"))
			demoPath += "//";
		if(!dataPath.endsWith("//"))
			dataPath += "//";
		Main.logger.log(-1,"Checked (" + demoPath + ")");
	}
	
	public static void logInfos(String testName, String lessID, String exID) {
		Main.logger.log(-1, "----------------------------");
		Main.logger.log(-1, testName);
		Main.logger.log(-1, "lesson\t: " + lessID);
		Main.logger.log(-1, "exercise\t: " + exID);
		Main.logger.log(-1, "----------------------------");
	}
	
// All about that file generation.
	public static void generateData(Collection<Lesson> cLess) {
		Iterator<Lesson> ite = cLess.iterator();
		try {
	        BufferedWriter bw = new BufferedWriter(new FileWriter(Generator.dataPath + "lessonList.json"));
	        bw.write("[");
	        try {
		        bw.write(lessonData((Lesson) ite.next()));
		        while(true) {
		        	Lesson s = (Lesson) ite.next();
		        	bw.write(",");
		        	bw.write(lessonData(s));
		        }
	        }
	        catch(NoSuchElementException e) {
	        	
	        }
	        finally {
		        bw.write("]");
		        bw.close();
	        }
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private static String lessonData(Lesson next) {
		JSONObject res = new JSONObject();
		res.put("id", next.getId());
		res.put("imgUrl", "assets/images/lessonIcons/" + next.getId() + "-icon.png");
		return res.toJSONString();
	}

	public static void generateData(String lessID, Collection<Lecture> cExo) {
		Iterator<Lecture> ite = cExo.iterator();
		try {
	        BufferedWriter bw = new BufferedWriter(new FileWriter(Generator.dataPath + lessID + "-list.json"));
	        bw.write("[");
	        try {
		        bw.write(lectureData(lessID, (Lecture) ite.next()));
		        while(true) {
		        	Lecture s = (Lecture) ite.next();
		        	bw.write(",");
		        	bw.write(lectureData(lessID, s));
		        }
	        }
	        catch(NoSuchElementException e) {
	        	
	        }
	        finally {
		        bw.write("]");
		        bw.close();
	        }
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static String lectureData(String lessID, Lecture lecture) {
		JSONArray a = new JSONArray();
		lectureDataRec(lessID, lecture, a);
		return a.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	private static void lectureDataRec(String originName, Lecture lecture, JSONArray a) {
		JSONObject res = new JSONObject();
		res.put("id", lecture.getId());
		res.put("parent", originName);
		a.add(res);
		for(Lecture l : lecture.getDependingLectures()) {
			lectureDataRec(lecture.getName(), l, a);
		}
	}

	@SuppressWarnings("unchecked")
	public static void generateData(String exo, Vector<World> worlds) {
		JSONObject r = new JSONObject();
		for(World w : worlds) {
			JSONObject wj = WorldParser.toJSON(w);
			if(wj != null)
				r.putAll(wj);
		}
		try {
	        BufferedWriter bw = new BufferedWriter(new FileWriter(Generator.worldPath + exo + ".json"));
	        bw.write(r.toJSONString());
	        bw.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
