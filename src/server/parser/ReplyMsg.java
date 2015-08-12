package server.parser;

import org.json.simple.JSONObject;
import org.xnap.commons.i18n.I18n;

import plm.core.model.lesson.ExecutionProgress;

/**
 * Generates a ReplyMsg form the 
 * @author Tanguy
 *
 */
public class ReplyMsg {

	String result;
	
	/**
	 * Generates a ReplyMsg from the internationalization parameter {@link I18n} and the last execution progress result.
	 * @param lastResult The last exercises' result.
	 * @param i18n The internationalization parameter for message output.
	 */
	@SuppressWarnings("unchecked")
	public ReplyMsg(ExecutionProgress lastResult, I18n i18n) {
		int type = lastResult.outcome == ExecutionProgress.outcomeKind.PASS ? 1 : 0;
		String msg = lastResult.getMsg(i18n);
		JSONObject res = new JSONObject();
		JSONObject gitReply = new JSONObject();
		// Create the GIT replies object.
		gitReply.put("lang", lastResult.language.toString());
		if (lastResult.outcome != null) {
			switch (lastResult.outcome) {
				case COMPILE:  gitReply.put("outcome", "compile");  break;
				case FAIL:     gitReply.put("outcome", "fail");     break;
				case PASS:     gitReply.put("outcome", "pass");     break;
				default:       gitReply.put("outcome", "UNKNOWN");  break;
			}
		}
		
		if (lastResult.totalTests > 0) {
			gitReply.put("passedtests", lastResult.passedTests + "");
			gitReply.put("totaltests", lastResult.totalTests + "");
		}
		
		if (lastResult.feedbackDifficulty != null)
			gitReply.put("exoDifficulty", lastResult.feedbackDifficulty);
		if (lastResult.feedbackInterest != null)
			gitReply.put("exoInterest", lastResult.feedbackInterest);
		if (lastResult.feedback != null)
			gitReply.put("exoComment", lastResult.feedback);
		if (lastResult.compilationError != null)
			gitReply.put("compilError", lastResult.compilationError);
		if (lastResult.executionError != null)
			gitReply.put("execError", lastResult.executionError);
		// create the log data.
		res.put("msgType", type);
		res.put("msg", msg);
		res.put("git_logs", gitReply);
		res.put("type", "result");
		res.put("commonErrorText", lastResult.commonErrorText);
		res.put("commonErrorID", lastResult.commonErrorID);
		result = res.toJSONString();
	}
	
	/**
	 * Outputs as a JSON-formatted {@link String}
	 * @return
	 */
	public String toJSON() {
		return result;
	}

}
