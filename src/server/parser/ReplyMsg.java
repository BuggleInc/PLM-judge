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
		res.put("type", "result");
		res.put("msgType", type);
		if (lastResult.outcome != null) {
			switch (lastResult.outcome) {
				case COMPILE:  res.put("outcome", "compile");  break;
				case FAIL:     res.put("outcome", "fail");     break;
				case PASS:     res.put("outcome", "pass");     break;
				default:       res.put("outcome", "UNKNOWN");  break;
			}
		}
		res.put("msg", msg);
		res.put("totaltests", lastResult.totalTests);
		res.put("passedtests", lastResult.passedTests);
		if (lastResult.compilationError != null)
			res.put("compilError", lastResult.compilationError);
		if (lastResult.executionError != null)
			res.put("execError", lastResult.executionError);
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
