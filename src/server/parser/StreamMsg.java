package server.parser;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import plm.universe.Operation;
import plm.universe.World;

public class StreamMsg {

	String result = "";
	public StreamMsg(World currWorld, List<Operation> operations) {
		JSONObject json = new JSONObject();
		JSONArray json_list = new JSONArray();
		for(Operation operation : operations) {
			JSONObject json_in = OperationParser.toJSON(operation);
			json_list.add(json_in);
		}
		json.put("worldID", currWorld.getName());
		json.put("operations", json_list);
		result = json.toJSONString();
	}

	public String toJSON() {
		return result;
	}

}