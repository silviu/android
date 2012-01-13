// This class handles JSON parsing

package lab.tutorial.restclient;


import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import android.media.audiofx.Equalizer;

public class JSONParser {
	// extract a device ID from a JSON description
	public static int extractId(JSONObject json) throws JSONException {
		return json.getInt("id");
	}

	// extract a device name from a JSON description
	public static String extractName(JSONObject json) throws JSONException {
		return json.getString("name");
	}

	// extract a device location from a JSON description
	public static String extractLocation(JSONObject json) throws JSONException {
		return json.getString("location");
	}
	
	// extract a device type from a JSON description
	public static String extractType(JSONObject json) throws JSONException {
		return json.getString("type");
	}
	
	// extract an actuator setting from a JSON description
	public static String extractSetting(JSONObject json) throws JSONException {
		return json.getString("setting");
	}
	
	// change an actuator setting and confirm
	public static Boolean confirmSetting(String URI, String args) throws JSONException, ClientProtocolException, ConnectTimeoutException, UnsupportedEncodingException {
		String response = RestComm.restPost(URI, args);
		if (response != null) {
			return response.equals("201");
		}
		return null;
	}
	
}
