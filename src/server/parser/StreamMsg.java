package server.parser;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import plm.universe.Operation;
import plm.universe.World;

/**
 * Acts as a link between {@link BasicListener} and {@link OperationParser}
 * @author Tanguy
 *
 */
public class StreamMsg {

	JSONObject result = null;
	@SuppressWarnings("unchecked")
	/**
	 * Creates a StreamMsg instance, to be handled before the JSON output is created.
	 * @param currWorld the current {@link World} object.
	 * @param operations List of {@link Operation} instances to be written.
	 */
	public StreamMsg(World currWorld, List<Operation> operations) {
		JSONObject json = new JSONObject();
		JSONArray json_list = new JSONArray();
		for(Operation operation : operations) {
			JSONObject json_in = OperationParser.toJSON(operation);
			json_list.add(json_in);
		}
		json.put("worldID", currWorld.getName());
		json.put("operations", json_list);
		result = json;
	}

	/**
	 * Returns this StreamMsg as a {@link String}.
	 * @return the JSON-formatted {@link String}.
	 */
	public String toJSON() {
		return result.toJSONString();
	}
	
	/**
	 * Returns this StreamMsg as a {@link JSONObject}.
	 * @return the {@link JSONObject}.
	 */
	public JSONObject result() {
		return result;
	}

}
