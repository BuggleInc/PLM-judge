package test;

import java.util.Locale;
import server.GameGest;
import server.Main;

public class ValidTests {
	private static GameGest gest = null;
	private static ConnectorTest connector = new ConnectorTest();
	private static int testID = 1;
	public void init() {
		connector.init("kappa", 1000);
		gest = new GameGest(connector, Main.logger);
	}
	
	public int initWithParams(String lessID, String exID, String code) {
		logInfos("Test "+ (testID++), lessID, exID, code);
		init();
		gest.setGameState(Locale.FRENCH, "Java", lessID, exID);
		gest.setCode(code);
		gest.startGame(30);
		return 1;
	}

	public static void main(String[] args) {
		Main.logger.log(-1, "Starting tests.");
		new ValidTests().initWithParams("lessons.welcome", "welcome.lessons.welcome.environment.Environment", "avance();");
		new ValidTests().initWithParams("lessons.welcome", "welcome.lessons.welcome.environment.Environment", "System.out.println(\"Test\");avance();");
		new ValidTests().initWithParams("lessons.sort.basic", "lessons.sort.basic", "public void bubbleSort()  {boolean stop = false; while(!stop) { stop = true; for(int i = 0; i < getValueCount()-1;i++) { if(isSmaller(i+1, i)) { swap(i+1, i); stop = false;}}}}");
	}
	
	public static void logInfos(String testName, String lessID, String exID, String code) {
		Main.logger.log(-1, "\n----------------------------");
		Main.logger.log(-1, testName);
		Main.logger.log(-1, "lesson\t: " + lessID);
		Main.logger.log(-1, "exercise\t: " + exID);
		Main.logger.log(-1, code);
		Main.logger.log(-1, "----------------------------");
	}
}
