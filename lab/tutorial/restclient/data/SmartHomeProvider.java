// ContentProvider for resources on the smart home server. Also has an SQLite local cache.

package lab.tutorial.restclient.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import lab.tutorial.restclient.JSONParser;
import lab.tutorial.restclient.actuators.ActuatorConstants;
import lab.tutorial.restclient.sensors.SensorConstants;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class SmartHomeProvider extends ContentProvider {

	// Remove server initialisation
	public static final String protocol = "http://";
	public static final String host = "embedded.cs.pub.ro/";
	public static final String statusURL = "si/zigbee/status";
	public static final String cmdURL = "si/zigbee/cmd";

	public static final String PROVIDER_NAME = "lab.tutorial.restclient.data.SmartHomeProvider";

	public static final Uri CONTENT_URI_SENSORS = Uri.parse("content://"+ PROVIDER_NAME + "/sensors");
	public static final Uri CONTENT_URI_SENSORVALUES = Uri.parse("content://"+ PROVIDER_NAME + "/values");
	public static final Uri CONTENT_URI_CURRENTVALUE = Uri.parse("content://"+ PROVIDER_NAME + "/value");
	public static final Uri CONTENT_URI_ACTUATORS = Uri.parse("content://"+ PROVIDER_NAME + "/actuators");
	public static final Uri CONTENT_URI_UPDATEACTUATOR = Uri.parse("content://"+ PROVIDER_NAME + "/actuator");

	// column names
	//for sensors
	public static final String _ID1 = "_id";
	public static final String extAddress = "extAddress";
	public static final String endpoint = "endpoint";
	public static final String clusterID = "clusterID";
	public static final String timestamp = "tstamp";
	public static final String location = "location";
	public static final String type = "type";



	//for sensor values
	public static final String _ID2 = "_id";
	public static final String attributes = "attributes";

	//for actuators
	public static final String _ID3 = "_id";
	public static final String SETTING = "setting";

	// routes
	private static final int SENSORS = 1;
	private static final int SENSORVALUES = 2;
	private static final int CURRENTVALUE = 3;
	private static final int ACTUATORS = 4;
	private static final int UPDATEACTUATOR = 5;

	// UriMatcher
	private static final UriMatcher uriMatcher;
	static{
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "sensors", SENSORS);
		uriMatcher.addURI(PROVIDER_NAME, "values/#", SENSORVALUES);
		uriMatcher.addURI(PROVIDER_NAME, "value/#", CURRENTVALUE);
		uriMatcher.addURI(PROVIDER_NAME, "actuators", ACTUATORS);
		uriMatcher.addURI(PROVIDER_NAME, "actuator/#", UPDATEACTUATOR);
	}

	//---for database use---
	private SQLiteDatabase smarthomeDB;
	private static final String DATABASE_NAME = "SmartHome";
	private static final String DATABASE_TABLE_SENSORS = "sensors";
	private static final String DATABASE_TABLE_SENSORVALUES = "valori";
	private static final String DATABASE_TABLE_ACTUATORS = "actuatori";
	private static final int DATABASE_VERSION = 1;

	private static class DatabaseHelper extends SQLiteOpenHelper 
	{
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
		}

		@Override
		public void onOpen(SQLiteDatabase db)
		{
			//start the downloader thread when the database is created
			DataDownloader downl = new DataDownloader();
			try {
				downl.initialDownload();
			} catch (ConnectTimeoutException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ClientProtocolException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Log.d("SMARTHOMEPROVIDER", "CREATING DATABASE");
			// Hack-ish initialisation. In a real case a smarter initialization is needed.
			db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_SENSORS);
			db.execSQL("CREATE TABLE " + DATABASE_TABLE_SENSORS + " (" + _ID1 + 
					" INTEGER PRIMARY KEY AUTOINCREMENT," + 
			"extAddress text, endpoint text, clusterID text, location text, type text, tstamp timestamp);");


			db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_SENSORVALUES);
			db.execSQL("CREATE TABLE " + DATABASE_TABLE_SENSORVALUES + " (" + _ID2 + 
					" INTEGER PRIMARY KEY AUTOINCREMENT," + 
			"extAddress text, endpoint text, attributes text, type text, tstamp timestamp);");


			db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_ACTUATORS);
			db.execSQL("CREATE TABLE " + DATABASE_TABLE_ACTUATORS + " (" + _ID2 + 
					" INTEGER PRIMARY KEY AUTOINCREMENT," + 
			"extAddress text, endpoint text, clusterID text, location text, type text, tstamp timestamp, setting text);");
			try {

				Log.d("SMARTHOMEPROVIDER", "ADDING DATA TO DATABASE");
				String senzori = downl.getDBSensors();
				db.execSQL("INSERT INTO "+DATABASE_TABLE_SENSORS+" "+senzori);


				String values = downl.getDBSensorValues();
				db.execSQL("INSERT INTO "+DATABASE_TABLE_SENSORVALUES+" "+values);
				
				String actuatori = downl.getDBActuators();
				db.execSQL("INSERT INTO "+DATABASE_TABLE_ACTUATORS+" "+actuatori);

			} catch (Exception e) {
				e.printStackTrace();
				Log.d("DatabaseDownloader", "Exceptie la adaugare in baza de date");
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, 
				int newVersion) {
			Log.w("Content provider database", 
					"Upgrading database from version " + 
					oldVersion + " to " + newVersion + 
			", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS sensors");
			db.execSQL("DROP TABLE IF EXISTS valori");
			onCreate(db);
		}
	}   

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)){
		//---get all sensors---
		case SENSORS:
			return PROVIDER_NAME + "/sensors";
			//---get all values of a sensor--
		case SENSORVALUES:
			return PROVIDER_NAME + "/values/#";
			//---get one value of a sensor--
		case CURRENTVALUE:
			return PROVIDER_NAME + "/value/#";
			//---get all actuators--
		case ACTUATORS:
			return PROVIDER_NAME + "/actuators";
		case UPDATEACTUATOR:
			return PROVIDER_NAME + "/actuator/#";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);        
		}   
	}

	@Override
	public boolean onCreate() {
		Context context = getContext();
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		smarthomeDB = dbHelper.getWritableDatabase();
		return (smarthomeDB == null)? false:true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();

		switch (uriMatcher.match(uri)){
		case SENSORS:
			sqlBuilder.setTables(DATABASE_TABLE_SENSORS);
			if (sortOrder==null || sortOrder=="") sortOrder = timestamp + " DESC";
			break;
		case SENSORVALUES:
			sqlBuilder.setTables(DATABASE_TABLE_SENSORVALUES);
			sqlBuilder.appendWhere(_ID3 + " = " + uri.getPathSegments().get(1));
			if (sortOrder==null || sortOrder=="") sortOrder = timestamp + " DESC";
			break;
		case CURRENTVALUE:
			sqlBuilder.setTables(DATABASE_TABLE_SENSORVALUES);
			sqlBuilder.appendWhere(_ID2 + " = " + uri.getPathSegments().get(1));
			break;
		case ACTUATORS:
			sqlBuilder.setTables(DATABASE_TABLE_ACTUATORS);
			if (sortOrder==null || sortOrder=="") sortOrder = timestamp + " DESC";
			break;
		default: throw new SQLException("Failed to process " + uri);
		}

		Cursor c = sqlBuilder.query(smarthomeDB, projection, selection, selectionArgs, null, null, sortOrder);

		//---register to watch a content URI for changes---
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) 
	{
		int changed = 0;
		Log.e("UPDATE", "INTRO");
		switch (uriMatcher.match(uri)){
		case UPDATEACTUATOR:
			Log.e("UPDATE", "CASE");
			String newValue = values.getAsString(SETTING); 
			String class_type = values.getAsString(type);
			
			//on/off switch cmd
			String serverUri = protocol + host + cmdURL;
			
			try {
				String response = JSONParser.confirmSetting(serverUri, selectionArgs[0], class_type);
				Log.e("UPDATE", response);

				if (response.equalsIgnoreCase(newValue)) {
					// setarea a fost schimbata pe server - o schimbam si local
					Log.e("UPDATE", "UPDATEEEEEEEEEEEED");
					changed = smarthomeDB.update(DATABASE_TABLE_ACTUATORS, values, "_id="+uri.getPathSegments().get(1), null);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		default: throw new SQLException("Failed to process " + uri);
		}
		return changed;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// we will not insert new sensors or actuators
		return null;
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// we will not delete sensors or actuators
		return 0;
	}
}
