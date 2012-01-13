// This class shows the UI for the Actuators app

package lab.tutorial.restclient;

import java.util.ArrayList;

import lab.tutorial.restclient.actuators.Actuator;
import lab.tutorial.restclient.actuators.OnOffSwitch;
import lab.tutorial.restclient.data.SmartHomeProvider;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
        		actuatori.add( new OnOffSwitch(c.getInt(c.getColumnIndex(SmartHomeProvider._ID3)), 
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
	    	    	OnOffSwitch o = (OnOffSwitch) actuatori.get(position);
	    	    	// AsyncTask to toggle the setting of OnOffSwitch o 
	    	    	new ToggleOnOff().execute(o);
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
                
                Actuator a = items.get(position);
                OnOffSwitch o = (OnOffSwitch)a;
                if (o != null) {
                        TextView t = (TextView) v.findViewById(R.id.txtName);
                        if (t != null) {
                              t.setText(Long.toString(o.getTimestamp()));                            
                        }
                        t = (TextView) v.findViewById(R.id.txtLocation);
                        if (t != null) {
                              t.setText(o.getLocation());                            
                        }
                        t = (TextView) v.findViewById(R.id.txtValue);
                        if (t != null) {
                        	  if (o.getSetting().equalsIgnoreCase("on")){
                        		  t.setText(o.onValue);
                        	  } else {
                        		  t.setText(o.offValue);
                        	  }
                        }
                        ImageView i = (ImageView) v.findViewById(R.id.img);
                        if (i != null) {
                        	if (o.getSetting().equalsIgnoreCase("on")){
                        		i.setImageResource(R.drawable.light_switch_on);
                        	} else {
                        		i.setImageResource(R.drawable.light_switch_off);
                        	}
                        }
                }
                return v;
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
	    	ContentValues editedValues = new ContentValues();
	        editedValues.put(SmartHomeProvider.SETTING, newValue);
	    	Uri actuatorChange = Uri.parse(provider+"/actuator/"+o.getId());
	        if (getContentResolver().update(actuatorChange,editedValues,null,null)!=0)
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