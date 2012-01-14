package lab.tutorial.restclient.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lab.tutorial.restclient.RestComm;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

class DataEntry
{
	int id;
	String extAddress;
	int endpoint;
	int clusterID;
	String attributes;
	long timestamp;
	public DataEntry(JSONObject o) throws JSONException
	{
		id = o.getInt("id");
		extAddress = o.getString("extAddress");
		endpoint = o.getInt("endpoint");
		clusterID = o.getInt("clusterID");
		attributes = o.getString("attributes");
		timestamp = o.getLong("timestamp");
	}
}


public class DataDownloader extends Thread 
{
	private Boolean first_run = true;
	private String URI = "http://embedded.cs.pub.ro/si/zigbee/status";
	private ArrayList<DataEntry> dentry_list = new ArrayList<DataEntry>();
	private Map<String, String> extAddress_to_location = new HashMap<String, String>();

	private static final int CLUSTER_ID_TEMP = 402;
	private static final int CLUSTER_ID_FLOW = 404;
	private static final int CLUSTER_ID_SWITCH = 7;
	private static final int CLUSTER_ID_TERMOSTAT = 201;
	private static final int CLUSTER_ID_BASIC = 0;


	public void run() 
	{
		Log.d("DataDownloader", "Starting Downloader THREAD");
		try {
			while(true) {

				Thread.sleep(100000);

				if (first_run) {
					initialDownload();
					first_run = false;
					Log.d("DataDownloader", "Finished initialdownload()");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("GetStatus", "run", e);
		}
	}

	public Boolean is_first_time()
	{
		return this.first_run;
	}

	public void initialDownload() throws ConnectTimeoutException, ClientProtocolException, JSONException
	{
		JSONObject json = RestComm.restGet(URI);
		Log.d("initialdownload", "Downloaded ALL statuses");
		JSONArray statusSet = json.getJSONArray("statusSet");
		for (int i = 0; i < statusSet.length(); i++)
		{
			try {
				JSONObject o = statusSet.getJSONObject(i);
				dentry_list.add(new DataEntry(o));
			}
			catch(Exception e){}
		}
	}


	public ArrayList<DataEntry> getByClusterId(int id)
	{
		int i = 0;
		ArrayList<DataEntry> entry_list = new ArrayList<DataEntry>();
		for (DataEntry d : dentry_list)
			if (d.clusterID == id && i < 50) {
				entry_list.add(d);
				i++;
			}
		return entry_list;
		
	}

	public ArrayList<DataEntry> getDEBasicClusters()
	{
		ArrayList<DataEntry> basic_clusters = new ArrayList<DataEntry>();
		basic_clusters.addAll(getByClusterId(CLUSTER_ID_BASIC));
		return basic_clusters;
	}

	
	public ArrayList<DataEntry> getDESensors()
	{
		ArrayList<DataEntry> sensor_list = new ArrayList<DataEntry>();
		sensor_list.addAll(getByClusterId(CLUSTER_ID_TEMP));
		sensor_list.addAll(getByClusterId(CLUSTER_ID_FLOW));

		return sensor_list;
	}

	public void parseLocations()
	{
		ArrayList<DataEntry> sensor_list = getDEBasicClusters();
		for (DataEntry sensor : sensor_list)
		{
			String[] split1 = sensor.attributes.split(",");
			String[] split2 = split1[2].substring(1, split1[2].length()-1).split(":");
			String location = split2[1];
			Log.e("LOCATION", location);
			extAddress_to_location.put(sensor.extAddress, location);
		}
	}

	public String getDBSensors()
	{
		parseLocations();
		ArrayList<DataEntry> sensor_list = getDESensors();

		String db_entry = new String();
		for (int i = 0; (i < sensor_list.size()); i++)
		{
			DataEntry curr = sensor_list.get(i);
			if (curr.extAddress.equalsIgnoreCase("null"))
				continue;

			String sensor_type = "nothing";
			if (curr.clusterID == 402)
				sensor_type = "temperature";
			else if ((curr.clusterID == 404))
				sensor_type = "flow";
			if (i == 0)
			{
				db_entry += "SELECT " + Integer.toString(curr.id) + " AS '_ID', '" +
				curr.extAddress + "' AS 'extAddress', '" +
				Integer.toString(curr.endpoint) + "' AS 'endpoint', '" +
				Integer.toString(curr.clusterID) + "' AS 'clusterID', '" +
				extAddress_to_location.get(curr.extAddress) + "' AS 'location', '"+
				sensor_type + "' AS 'type', '" +
				Long.toString(curr.timestamp) + "' AS 'tstamp' ";

			}
			else
			{
				db_entry += "UNION SELECT  " +  Integer.toString(curr.id) + " AS '_ID', '" +
				curr.extAddress + "', '" +
				Integer.toString(curr.endpoint) + "', '" +
				Integer.toString(curr.clusterID) + "', '" +
				extAddress_to_location.get(curr.extAddress) + "', '"+
				sensor_type + "', '" + 
				Long.toString(curr.timestamp) + "'";


			}

		}
		return db_entry;
	}


	public String getDBSensorValues()
	{
		ArrayList<DataEntry> sensor_list = getDESensors();

		String db_entry = new String();
		Log.e("SENZOOOOOR SIZE", Integer.toString(sensor_list.size()));
		for (int i = 0; i < sensor_list.size(); i++)
		{
			DataEntry curr = sensor_list.get(i);

			if (curr.extAddress.equalsIgnoreCase("null"))
				continue;

			String sensor_type = "nothing";
			if (curr.clusterID == 402)
				sensor_type = "temperature";
			else if (curr.clusterID == 404)
				sensor_type = "flow";



			String no_accolades = curr.attributes.substring(1, curr.attributes.length()-1);
			String[] split_values = no_accolades.split(":");
			String str_val = split_values[1];
			double val = 0;
			if (curr.clusterID == 402)
			{
				int tmp = Integer.parseInt(str_val, 16);
				val = ((double)tmp/100);
			}
			else {
				int tmp = Integer.parseInt(str_val, 16);
				val = ((double)tmp/10);
			}

			if (i == 0)
			{
				db_entry += "SELECT " + Integer.toString(curr.id) + " AS '_ID', '" +
				curr.extAddress + "' AS 'extAddress', '" +
				Integer.toString(curr.endpoint) + "' AS 'endpoint', '" +
				Double.toString(val) + "' AS 'attributes', '" +
				sensor_type + "' AS 'type', '" +
				Long.toString(curr.timestamp) + "' AS 'tstamp' ";

			}
			else
			{
				db_entry += "UNION SELECT  " +  Integer.toString(curr.id) + " AS '_ID', '" +
				curr.extAddress + "', '" +
				Integer.toString(curr.endpoint) + "', '" +
				Double.toString(val) + "', '" +
				sensor_type + "', '" +
				Long.toString(curr.timestamp) + "'";
			}	

		}
		return db_entry;
	}

	public ArrayList<DataEntry> getDEActuators()
	{
		ArrayList<DataEntry> actuator_list = new ArrayList<DataEntry>();
		actuator_list.addAll(getByClusterId(CLUSTER_ID_SWITCH));
		actuator_list.addAll(getByClusterId(CLUSTER_ID_TERMOSTAT));
		return actuator_list;
	}

	public String getDBActuators()
	{
		ArrayList<DataEntry> actuator_list = getDEActuators();

		String db_entry = new String();
		for (int i = 0; i < actuator_list.size(); i++)
		{
			DataEntry curr = actuator_list.get(i);

			if (curr.extAddress.equalsIgnoreCase("null"))
				continue;

			String setting;
			String actuator_type;
			//on/off switch
			if (curr.clusterID == CLUSTER_ID_SWITCH)
			{
				String first_attribute = curr.attributes.split(",")[0];
				String hexa = first_attribute.substring(1, first_attribute.length()-1).split(":")[1];
				int val = Integer.parseInt(hexa, 16);
				if (val == 0)
					setting = "off";
				else
					setting = "on";
				actuator_type = "switch";
			}
			// Thermostat
			else
			{
				String thermostat_attribute = curr.attributes;
				String common = thermostat_attribute.substring(2, thermostat_attribute.length()-2);

				String[] min_partial = common.split(",");
				if (min_partial.length < 2)
					continue;
				String good = min_partial[0].split(":")[1];

				String min_str = good.substring(0, good.length()-1);

				double min = ((double)Integer.parseInt(min_str, 16))/100;
				
				String max_partial = common.split(",")[1];
				String max_str = max_partial.split(":")[1];
				double max = ((double)Integer.parseInt(max_str, 16))/100;
				
				setting = Double.toString(min) + "#" + Double.toString(max);
				actuator_type = "thermostat";

			}
			
			if (i == 0)
			{
				db_entry += "SELECT " + Integer.toString(curr.id) + " AS '_ID', '" +
				curr.extAddress + "' AS 'extAddress', '" +
				Integer.toString(curr.endpoint) + "' AS 'endpoint', '" +
				Integer.toString(curr.clusterID) + "' AS 'clusterID', '" +
				extAddress_to_location.get(curr.extAddress) + "' AS 'location', '"+
				actuator_type + "' AS 'type', '" +
				Long.toString(curr.timestamp) + "' AS 'tstamp', '" +
				setting + "' AS 'setting' ";
			}
			else
			{
				db_entry += "UNION SELECT  " +  Integer.toString(curr.id) + " AS '_ID', '" +
				curr.extAddress + "', '" +
				Integer.toString(curr.endpoint) + "', '" +
				Integer.toString(curr.clusterID) + "', '" +
				extAddress_to_location.get(curr.extAddress) + "', '"+
				actuator_type + "', '" +
				Long.toString(curr.timestamp) + "', '" +
				setting + "'";
			}

		}
		return db_entry;
	}

}