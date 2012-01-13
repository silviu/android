// This class handles JSON parsing

package lab.tutorial.restclient;

import lab.tutorial.restclient.actuators.ActuatorConstants;
import lab.tutorial.restclient.sensors.SensorConstants;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
	public static String confirmSetting(String URI) throws JSONException, ClientProtocolException, ConnectTimeoutException {
		JSONObject response = RestComm.restPut(URI);
		if (response != null) {
			return response.getJSONObject("actuator").getString("setting");
		}
		return null;
	}
	
	// get list of sensors
	public static String getSensors(String URI, String type) throws JSONException, ClientProtocolException, ConnectTimeoutException {
	
		String senzori = new String();
		JSONObject response = RestComm.restGet(URI);
		if (response != null) {
			JSONArray sList = response.getJSONArray(SensorConstants.SENSOR_SET);
			for (int i = 0; i < sList.length();i++) {
				// Daca este un senzor de temperatura il adaugam in lista ca atare
				if (JSONParser.extractType(sList.getJSONObject(i)).equalsIgnoreCase(type)){
					JSONObject j = sList.getJSONObject(i);
					if (i==0) senzori+= "SELECT "+extractId(j)+ " AS '_ID', '"
						                          +extractName(j)+ "' AS 'name', '"
												  +extractLocation(j)+ "' AS 'location', '"
												  +extractType(j)+ "' AS 'type' ";
					else senzori+= "UNION SELECT  "+extractId(j)+ " AS '_ID', '"
												+extractName(j)+ "', '" 
												+extractLocation(j)+ "', '" 
												+extractType(j)+ "'";
				}
			}
		} 
		return senzori;
	}
	
	// get list of actuators
	public static String getActuators(String URI, String type) throws JSONException, ClientProtocolException, ConnectTimeoutException {
		
		String actuatori = new String();
		JSONObject response = RestComm.restGet(URI);
		if (response != null) {
			JSONArray aList = response.getJSONArray(ActuatorConstants.ACTUATOR_SET);
			for (int i = 0; i < aList.length();i++) {
				// Daca este un senzor de temperatura il adaugam in lista ca atare
				if (JSONParser.extractType(aList.getJSONObject(i)).equalsIgnoreCase(type)){
					JSONObject j = aList.getJSONObject(i);
					if (i==0) actuatori+= "SELECT "+extractId(j)+ " AS '_ID', '"
						                          +extractName(j)+ "' AS 'name', '"
												  +extractLocation(j)+ "' AS 'location', '"
												  +extractType(j)+ "' AS 'type', '"
												  +extractSetting(j)+ "' AS 'setting' ";
					else actuatori+= "UNION SELECT  "+extractId(j)+ " AS '_ID', '"
												+extractName(j)+ "', '" 
												+extractLocation(j)+ "', '"
												+extractType(j)+ "', '"
												+extractSetting(j)+ "'";
				}
			}
		}
		return actuatori;
	}
	
	// get list of values for a sensor
	public static String getSensorValues(String URI) throws JSONException, ClientProtocolException, ConnectTimeoutException {
		
		String values = new String();
		JSONObject response = RestComm.restGet(URI);
		if (response != null) {
			JSONArray vList = response.getJSONArray(SensorConstants.VALUE_SET);
			for (int i = 0; i < vList.length();i++) {
				JSONObject j = vList.getJSONObject(i);
				if (i==0) values+= "SELECT "+j.getInt("id")+ " AS '_ID', '"
											  +j.getString("idSenzor")+ "' AS 'idSenzor', '"
					                          +j.getString("timestamp")+ "' AS 'timestamp', '"
											  +j.getDouble("valoare")+ "' AS 'value' ";
				else values+= "UNION SELECT  "+j.getInt("id")+ " AS '_ID', '"
											+j.getString("idSenzor")+ "', '"
											+j.getString("timestamp")+ "', '" 
											+j.getDouble("valoare")+ "'";
			}
		}
		return values;
	}
	
}
