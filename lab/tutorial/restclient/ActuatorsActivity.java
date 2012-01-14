// This class shows the UI for the Actuators app

package lab.tutorial.restclient;

import java.util.ArrayList;

import lab.tutorial.restclient.actuators.Actuator;
import lab.tutorial.restclient.actuators.OnOffSwitch;
import lab.tutorial.restclient.actuators.Thermostat;
import lab.tutorial.restclient.data.SmartHomeProvider;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ActuatorsActivity extends Activity {

	// Local data
	private String provider = "content://lab.tutorial.restclient.data.SmartHomeProvider";
	private ArrayList<Actuator> actuatori;
	// UI elements
	private ListView lv;
	private double new_thermostat_min = -1;
	private double new_thermostat_max = -1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// UI initialisation
		setContentView(R.layout.actuators);
		lv = (ListView) findViewById(R.id.listView);

		// Read data from server
		actuatori = new ArrayList<Actuator>();
		Uri allActuators = Uri.parse(provider+"/actuators");
		Cursor c = managedQuery(allActuators, null, null, null, null);
		if (c.moveToFirst()) {
			do{
				if (c.getString(c.getColumnIndex(SmartHomeProvider.type)).equalsIgnoreCase("switch"))
					actuatori.add( new OnOffSwitch(c.getInt(c.getColumnIndex(SmartHomeProvider._ID3)), 
							c.getString(c.getColumnIndex(SmartHomeProvider.extAddress)),
							c.getString(c.getColumnIndex(SmartHomeProvider.endpoint)),
							c.getString(c.getColumnIndex(SmartHomeProvider.clusterID)),
							c.getString(c.getColumnIndex(SmartHomeProvider.location)),
							c.getLong(c.getColumnIndex(SmartHomeProvider.timestamp)),
							c.getString(c.getColumnIndex(SmartHomeProvider.SETTING))));
				else
					actuatori.add( new Thermostat(c.getInt(c.getColumnIndex(SmartHomeProvider._ID3)), 
							c.getString(c.getColumnIndex(SmartHomeProvider.extAddress)),
							c.getString(c.getColumnIndex(SmartHomeProvider.endpoint)),
							c.getString(c.getColumnIndex(SmartHomeProvider.clusterID)),
							c.getString(c.getColumnIndex(SmartHomeProvider.location)),
							c.getLong(c.getColumnIndex(SmartHomeProvider.timestamp)),
							c.getString(c.getColumnIndex(SmartHomeProvider.SETTING))));

			} while (c.moveToNext());
		}

		// Configure UI for the sensor list
		final ListWithImageAdapter la = new ListWithImageAdapter(this,R.layout.list_item, actuatori);
		lv.setAdapter(la);
		// What happens when you click a button in the Sensor list
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Actuator o = actuatori.get(position);
				// AsyncTask to toggle the setting of OnOffSwitch o 
				if (o.getType().equalsIgnoreCase("switch"))
					new ToggleOnOff().execute((OnOffSwitch)o);
				else{
					new_thermostat_min = -1;
					new_thermostat_max = -1;
					thermostat_read_text((Thermostat)o);
				}
			}
		});
	}

	// What happens when you press the back button on your phone
	@Override
	public void onBackPressed() {
	}

	// What happens when you press the menu button on your phone
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
	// What happens when you press a button in the Options menu
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.sensors:
			Intent sIntent = new Intent(this, SensorsActivity.class);
			startActivity(sIntent);
			return true;
		case R.id.actuators:
			Intent aIntent = new Intent(this, ActuatorsActivity.class);
			startActivity(aIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// Custom adapter to populate the sensors list
	private class ListWithImageAdapter extends ArrayAdapter<Actuator> {

		private ArrayList<Actuator> items;

		public ListWithImageAdapter(Context context, int textViewResourceId, ArrayList<Actuator> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.list_item, null);
			}

			Actuator o = items.get(position);
			if (o != null) {
				TextView t = (TextView) v.findViewById(R.id.txtName);
				if (t != null) {
					if (o.getType().equalsIgnoreCase("switch"))
						t.setText("ON/OFF Switch");
					else
						t.setText("Thermostat");
				}
				t = (TextView) v.findViewById(R.id.txtLocation);
				if (t != null) {
					t.setText(o.getLocation());                            
				}
				t = (TextView) v.findViewById(R.id.txtValue);
				if (t != null) {
					if (o.getType().equalsIgnoreCase("switch"))
					{
						if (o.getSetting().equalsIgnoreCase("on")){
							t.setText(((OnOffSwitch)o).onValue);
						} else {
							t.setText(((OnOffSwitch)o).offValue);
						}
					}
					else
					{
						t.setText(((Thermostat)o).getminVal() + "-" + ((Thermostat)o).getmaxVal());
					}
				}
				ImageView i = (ImageView) v.findViewById(R.id.img);
				if (i != null) {
					if (o.getType().equalsIgnoreCase("switch"))
					{
						if (o.getSetting().equalsIgnoreCase("on")){
							i.setImageResource(R.drawable.light_switch_on);
						} else {
							i.setImageResource(R.drawable.light_switch_off);
						}
					}
				}
			}
			return v;
		}
	}
	
	private void thermostat_read_text(final Thermostat o) 
	{
		final AlertDialog alertDialog;
		alertDialog = new AlertDialog.Builder(this).create();  
		// Create the text field in the alert dialog...

		LinearLayout layout= new LinearLayout(this);
		layout.setOrientation(1); //1 is for vertical orientation

		final EditText min_field = new EditText(this);
		min_field.setSingleLine();
		min_field.setInputType(0x00002002);
		min_field.setHint("Min temp");

		final EditText max_field = new EditText(this);
		max_field.setSingleLine();
		max_field.setInputType(0x00002002);
		max_field.setHint("Max temp");

		
		final OnClickListener mCorkyListener = new OnClickListener() {
		    public void onClick(View v) {
		      alertDialog.dismiss();
		    }
		};

		
		Button cancel = new Button(this);
		cancel.setOnClickListener(mCorkyListener);
		cancel.setText("Cancel");

		layout.addView(min_field);
		layout.addView(max_field);
		layout.addView(cancel);
		alertDialog.setView(layout);
		
		
		// Add text to dialog
		alertDialog.setMessage("Please set the thermostat.");  
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int which) {
				//save_therm_vals(min_field.getText().toString(), max_field.getText().toString());
				if (min_field.getText().toString().isEmpty())
					return;
				
				if (max_field.getText().toString().isEmpty())
					return;
				
				new_thermostat_min = Double.parseDouble(min_field.getText().toString());
				new_thermostat_max = Double.parseDouble(max_field.getText().toString());
				new ThermostatUpdate().execute(o);
			}
		});         

		alertDialog.show();
	}
	
	
	private class ThermostatUpdate extends AsyncTask<Thermostat, Void, Boolean> {

		private Thermostat o;
		private String newValue;
		protected Boolean doInBackground(Thermostat... os) {
			o = os[0];
			
			String newValue = Double.toString(new_thermostat_min) + "#" +
							  Double.toString(new_thermostat_max);
			
			String new_min = Integer.toHexString((int)(new_thermostat_min * 100));
			String new_max = Integer.toHexString((int)(new_thermostat_max * 100));

			String attribute = "[{0015:" + new_min + "},{0016:" + new_max + "}]";

			
			ContentValues editedValues = new ContentValues();
			editedValues.put(SmartHomeProvider.SETTING, newValue);
			editedValues.put(SmartHomeProvider.type, "thermostat");

			Uri actuatorChange = Uri.parse(provider+"/actuator/"+o.getId());
			String[] params = new String[5];
			params[0]= o.getExtAddress() + "#" + o.getEndpoint() + "#" + 
			o.getClusterID()  + "#" + attribute  + "#" + 
			Long.toString(o.getTimestamp() + 1);
			Log.e("PARAMS", params[0]);

			if (getContentResolver().update(actuatorChange,editedValues,null,params)!=0)
				return true;
			return false;
		}

		protected void onPostExecute(Boolean result) {
			if (result){
				o.setminVal(new_thermostat_min);
				o.setmaxVal(new_thermostat_max);
				((ListWithImageAdapter) lv.getAdapter()).notifyDataSetChanged();
			} else {
				Toast.makeText(getApplicationContext(), "Actuator setting change failed", Toast.LENGTH_SHORT);
			}
		}
	}
	

	// custom AsyncTask that is called when an actuator is tapped in order to toggle its setting
	private class ToggleOnOff extends AsyncTask<OnOffSwitch, Integer, Boolean> {

		private OnOffSwitch o;
		private String newValue;
		protected Boolean doInBackground(OnOffSwitch... os) {
			o = os[0];
			newValue = o.onValue;
			if (o.getSetting().equalsIgnoreCase("on")) newValue = o.offValue;
			String url_val;
			if (newValue.equalsIgnoreCase("on"))
				url_val = "01";
			else
				url_val = "00";
			String attribute = "{0000:" + url_val + "}";
			ContentValues editedValues = new ContentValues();
			editedValues.put(SmartHomeProvider.SETTING, newValue);
			editedValues.put(SmartHomeProvider.type, "switch");
			
			Uri actuatorChange = Uri.parse(provider+"/actuator/"+o.getId());
			String[] params = new String[5];
			params[0]= o.getExtAddress() + "#" + o.getEndpoint() + "#" + 
			o.getClusterID()  + "#" + attribute         + "#" + 
			Long.toString(o.getTimestamp() + 1);
			Log.e("PARAMS", params[0]);
			if (getContentResolver().update(actuatorChange,editedValues,null,params)!=0)
				return true;
			return false;
		}

		protected void onPostExecute(Boolean result) {
			if (result){
				o.setSetting(newValue);
				((ListWithImageAdapter) lv.getAdapter()).notifyDataSetChanged();
			} else {
				Toast.makeText(getApplicationContext(), "Actuator setting change failed", Toast.LENGTH_SHORT);
			}
		}
	}
}