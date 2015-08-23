package server.parser;

import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import server.Main;

/**
 * Parses a JSON request message (in the form of a {@link String}) into 
 * @author Tanguy
 *
 */
public class RequestMsg {

	private String lessonID;
	private String exerciseID;
	private String loc;
	private String lang;
	private String code;
	private String replyQueue;
	
	private RequestMsg() {
		// NO OP
	}
	
	/**
	 * Factory. Retrieves the data from the given message.
	 * @param s The message, as a JSON-formatted {@link String}
	 * @return a filled RequestMsg.
	 */
	public static RequestMsg readMessage(String s) {
		RequestMsg replyData = new RequestMsg();
		JSONParser p = new JSONParser();
		try {
			JSONObject replyJSON = (JSONObject) p.parse(s);
			replyData.lessonID = (String) replyJSON.get("lesson");
			replyData.exerciseID = (String) replyJSON.get("exercise");
			replyData.loc = (String) replyJSON.get("localization");
			replyData.lang = (String) replyJSON.get("language");
			replyData.code = (String) replyJSON.get("code");
			replyData.replyQueue = (String) replyJSON.get("replyQueue");
		} catch (ParseException e) {
			Main.logger.log(2, "Parse exception : message in queue didn't fit the expected format.");
			e.printStackTrace();
		}
		return replyData;
	}

	/**
	 * Retrieves the messages' Lesson ID
	 * @return the PLM-compliant lessonID.
	 */
	public String getLessonID() {
		return lessonID;
	}

	/**
	 * Retrieves the messages' Exercise ID.
	 * @return the PLM-compliant exerciseID.
	 */
	public String getExerciseID() {
		return exerciseID;
	}

	/**
	 * Retrieves the messages' localization.
	 * @return the PLM-compliant natural language.
	 */
	public String getLocale() {
		return loc;
	}

	/**
	 * Retrieves the messages' language.
	 * @return the PLM-compliant programming language.
	 */
	public String getLanguage() {
		return lang;
	}

	/**
	 * Retrieves the messages' code.
	 * @return the PLM-compliant code.
	 */
	public String getCode() {
		return code;
	}
	
	public String getReplyQueue() {
		return replyQueue;
	}
}
