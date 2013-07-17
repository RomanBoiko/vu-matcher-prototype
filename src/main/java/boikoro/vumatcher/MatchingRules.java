package boikoro.vumatcher;

import org.json.JSONException;
import org.json.JSONObject;

public class MatchingRules {
	public static boolean isMatchedPair(JSONObject activeObject, JSONObject potentialObject) throws JSONException {
		return 
				activeObject.getInt("amount")==potentialObject.getInt("amount")
				&&
				!activeObject.getString("buy_sell").equals(potentialObject.getString("buy_sell"))
				&&
				!activeObject.getString("book").equals(potentialObject.getString("book"));
	}
}
