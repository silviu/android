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
	public ArrayList<DataEntry> dentry_list = new ArrayList<DataEntry>();
	Map<String, String> extAddress_to_location = new HashMap<String, String>();


	public static final int CLUSTER_ID_TEMP = 402;
	public static final int CLUSTER_ID_FLOW = 404;
	public static final int CLUSTER_ID_SWITCH = 7;
	public static final int CLUSTER_ID_TERMOSTAT = 201;
	public static final int CLUSTER_ID_BASIC = 0;


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
		ArrayList<DataEntry> entry_list = new ArrayList<DataEntry>();
		for (DataEntry d : dentry_list)
			if (d.clusterID == id)
				entry_list.add(d);
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
			extAddress_to_location.put(sensor.extAddress, location);
		}
	}

	public String getDBSensors()
	{
		parseLocations();
		ArrayList<DataEntry> sensor_list = getDESensors();

		String db_entry = new String();
		for (int i = 0; (i < sensor_list.size() && i < 50); i++)
		{
			DataEntry curr = sensor_list.get(i);
			if (i == 0)
			{
				db_entry += "SELECT " + Integer.toString(curr.id) + " AS '_ID', '" +
				curr.extAddress + "' AS 'extAddress', '" +
				Integer.toString(curr.endpoint) + "' AS 'endpoint', '" +
				Integer.toString(curr.clusterID) + "' AS 'clusterID', '" +
				extAddress_to_location.get(curr.extAddress) + "' AS 'location' ";
			}
			else
			{
				db_entry += "UNION SELECT  " +  Integer.toString(curr.id) + " AS '_ID', '" +
				curr.extAddress + "', '" +
				Integer.toString(curr.endpoint) + "', '" +
				Integer.toString(curr.clusterID) + "', '" +
				extAddress_to_location.get(curr.extAddress) + "'";

			}

		}
		return db_entry;
	}


	public String getDBSensorValues()
	{
		ArrayList<DataEntry> sensor_list = getDESensors();

		String db_entry = new String();
		Log.e("SENZOOOOOR SIZE", Integer.toString(sensor_list.size()));
		for (int i = 0; (i < sensor_list.size() && i < 50); i++)
		{
			DataEntry curr = sensor_list.get(i);

			String no_accolades = curr.attributes.substring(1, curr.attributes.length()-1);
			String[] split_values = no_accolades.split(":");
			String str_val = split_values[1];
			int val = 0;
			if (curr.clusterID == 402)
				val = Integer.parseInt(str_val, 16)/100;
			else
				val = Integer.parseInt(str_val, 16)/10;

			if (i == 0)
			{
				db_entry += "SELECT " + Integer.toString(curr.id) + " AS '_ID', '" +
				curr.extAddress + "' AS 'extAddress', '" +
				Integer.toString(curr.endpoint) + "' AS 'endpoint', '" +
				Integer.toString(val) + "' AS 'attributes', '" +
				Long.toString(curr.timestamp) + "' AS 'timestamp' ";

			}
			else
			{
				db_entry += "UNION SELECT  " +  Integer.toString(curr.id) + " AS '_ID', '" +
				curr.extAddress + "', '" +
				Integer.toString(curr.endpoint) + "', '" +
				Integer.toString(val) + "', '" +
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
		for (int i = 0; (i < actuator_list.size() && i < 500); i++)
		{
			DataEntry curr = actuator_list.get(i);
			if (i == 0)
			{
				db_entry += "SELECT " + Integer.toString(curr.id) + " AS '_ID', '" +
				curr.extAddress + "' AS 'extAddress', '" +
				Integer.toString(curr.endpoint) + "' AS 'endpoint', '" +
				Integer.toString(curr.clusterID) + "' AS 'clusterID', '" +
				Long.toString(curr.timestamp) + "' AS 'timestamp', '" +
				extAddress_to_location.get(curr.extAddress) + "' AS 'location', '"+
				curr.attributes + "' AS 'setting' ";
			}
			else
			{
				db_entry += "UNION SELECT  " +  Integer.toString(curr.id) + " AS '_ID', '" +
				curr.extAddress + "', '" +
				Integer.toString(curr.endpoint) + "', '" +
				Integer.toString(curr.clusterID) + "', '" +
				Long.toString(curr.timestamp) + "', '" +
				extAddress_to_location.get(curr.extAddress) + "', '"+
				curr.attributes + "'";
			}

		}
		return db_entry;
	}

}