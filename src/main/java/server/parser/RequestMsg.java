package main.java.server.parser;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import plm.core.log.Logger;

/**
 * Parses a JSON request message (in the form of a {@link String}) into 
 * @author Tanguy
 *
 */
public class RequestMsg {

	private String jsonExercise;
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
			replyData.jsonExercise = (String) replyJSON.get("exercise");
			replyData.loc = (String) replyJSON.get("localization");
			replyData.lang = (String) replyJSON.get("language");
			replyData.code = (String) replyJSON.get("code");
			replyData.replyQueue = (String) replyJSON.get("replyQueue");
		} catch (ParseException e) {
			Logger.log(2, "Parse exception : message in queue didn't fit the expected format.");
			e.printStackTrace();
		}
		return replyData;
	}

	public String getJSONExercise() {
		return jsonExercise;
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
