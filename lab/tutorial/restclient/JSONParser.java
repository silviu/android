// This class handles JSON parsing

package lab.tutorial.restclient;


import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import android.media.audiofx.Equalizer;
import android.util.Log;

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
	public static String confirmSetting(String URI, String args, String type) throws JSONException, ClientProtocolException, ConnectTimeoutException, UnsupportedEncodingException {
		String response = RestComm.restPost(URI, args);
		Log.e("RESPONSE", response);
		if (response != null) {
			if (type.equalsIgnoreCase("switch"))
			{
				String with_accolades = response.split(",")[4];
				String accol = with_accolades.split(":")[1] + ":" + with_accolades.split(":")[2];
				String no_accolades = accol.substring(2, accol.length()-2);
				String onoff = no_accolades.split(":")[1];

				if (onoff.equals("00"))
					return "off";
				else
					return "on";
			}
			else {
				//[{0015:02BC},{0016:0BB8}]
				String with_accolades = response.split(",")[4] + response.split(",")[5];
				String first = with_accolades.split(":")[2];
				String updated_min = first.substring(0, first.indexOf('}'));
				String final_min = Integer.toString(Integer.parseInt(updated_min, 16));
				double min_val = ((double)Integer.parseInt(final_min)/100);
				
				String second = with_accolades.split(":")[3];
				String updated_max = second.substring(0, first.indexOf('}'));
				String final_max = Integer.toString(Integer.parseInt(updated_max, 16));
				double max_val = ((double)Integer.parseInt(final_max)/100);

				
				return Double.toString(min_val) + "#" + Double.toString(max_val);
			}
		}
		return null;
	}

}
