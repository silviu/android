// This class shows the UI for the Sensors app

package lab.tutorial.restclient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

import lab.tutorial.restclient.data.SmartHomeProvider;
import lab.tutorial.restclient.sensors.Sensor;
import lab.tutorial.restclient.sensors.SensorConstants;
import lab.tutorial.restclient.sensors.TemperatureSensor;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
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
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

import com.androidplot.Plot;
import com.androidplot.series.XYSeries;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

public class SensorsActivity extends Activity {
	
	// Local data
	private String provider = "content://lab.tutorial.restclient.data.SmartHomeProvider";
	ArrayList<Sensor> senzori;
	// UI elements
	private ListView lv;
	private XYPlot mySimpleXYPlot;
	private ViewFlipper flipper;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // UI initialisation
        setContentView(R.layout.sensors);
        flipper = (ViewFlipper) findViewById(R.id.flipper);
        lv = (ListView) findViewById(R.id.listView);
        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
        
        // Read data from local cache
        senzori = new ArrayList<Sensor>();
        Uri allSensors = Uri.parse(provider+"/sensors");
        Cursor c = managedQuery(allSensors, null, null, null, null);
        if (c.moveToFirst()) {
        	do{
        		senzori.add( new TemperatureSensor(c.getInt(c.getColumnIndex(SmartHomeProvider._ID1)), 
        						c.getString(c.getColumnIndex(SmartHomeProvider.extAddress)),
        						c.getString(c.getColumnIndex(SmartHomeProvider.endpoint)),
        						c.getString(c.getColumnIndex(SmartHomeProvider.clusterID)),
        						c.getLong(c.getColumnIndex(SmartHomeProvider.timestamp))));
        	} while (c.moveToNext());
        }
        
        // Configure UI for the sensor list
        final ListWithImageAdapter la = new ListWithImageAdapter(this,R.layout.list_item, senzori);
        lv.setAdapter(la);
        
        //TODO: De reparat graficele. se asteapta la numere reale dde la Smarthomeprovider.VALUE, 
        		//dar eu am un String attributes
        
        // What happens when you click a button in the Sensor list
       /* lv.setOnItemClickListener(new OnItemClickListener() {
    	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	    	flipper.showNext();
    	    	TemperatureSensor s = (TemperatureSensor)senzori.get(position);
        		Number[] timestamps = new Number[5];
        		Number[] values = new Number[5];
    	    	Uri allValues = Uri.parse(provider+"/values/"+s.getId());
    	        Cursor c = managedQuery(allValues, null, null, null, null);
    	        int i=0;
    	        if (c.moveToFirst()) {
    	        	do{
    	        		// Difference between SQL Timestamp and Java Timestamp, got to *1000
    	        		timestamps[i] = new Long(c.getLong(c.getColumnIndex(SmartHomeProvider.timestamp))*1000);
    	        		values[i] = new Double(c.getDouble(c.getColumnIndex(SmartHomeProvider.VALUE)));
    	        	} while (c.moveToNext() && ++i<5);
    	        }
    	    	createChart(timestamps, values, s);
    	    }
        });
        */
    }
    
    private void createChart(Number[] timestamps, Number[] values, TemperatureSensor s){
    	mySimpleXYPlot.clear();

    	// create our series from our array of nums:
        XYSeries series = new SimpleXYSeries(
                Arrays.asList(timestamps),
                Arrays.asList(values),
                "Temperatura de la senzor");
 
        mySimpleXYPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        mySimpleXYPlot.getGraphWidget().getGridLinePaint().setColor(Color.BLACK);
        mySimpleXYPlot.getGraphWidget().getGridLinePaint().setPathEffect(new DashPathEffect(new float[]{1,1}, 1));
        mySimpleXYPlot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        mySimpleXYPlot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);
        mySimpleXYPlot.getGraphWidget().getDomainLabelPaint().setTextSize(16);
        mySimpleXYPlot.getGraphWidget().getRangeLabelPaint().setTextSize(16);
        mySimpleXYPlot.getGraphWidget().getCursorLabelPaint().setTextSize(16);
 
        mySimpleXYPlot.setBorderStyle(Plot.BorderStyle.SQUARE, null, null);
        mySimpleXYPlot.getBorderPaint().setStrokeWidth(1);
        mySimpleXYPlot.getBorderPaint().setAntiAlias(false);
        mySimpleXYPlot.getBorderPaint().setColor(Color.WHITE);
 
        // setup our line fill paint to be a slightly transparent gradient:
        Paint lineFill = new Paint();
        lineFill.setAlpha(200);
        lineFill.setShader(new LinearGradient(0, 0, 0, 250, Color.WHITE, Color.GREEN, Shader.TileMode.MIRROR));
 
        LineAndPointFormatter formatter  = new LineAndPointFormatter(Color.rgb(0, 0,0), Color.BLUE, Color.RED);
        formatter.setFillPaint(lineFill);
        mySimpleXYPlot.getGraphWidget().setPadding(2, 2, 15, 10);
        mySimpleXYPlot.addSeries(series, formatter);
        mySimpleXYPlot.setRangeBoundaries(-5, 40, BoundaryMode.FIXED);
        
        // draw a domain tick for each entry:
        mySimpleXYPlot.setDomainStep(XYStepMode.SUBDIVIDE, timestamps.length);
 
        // customize our domain/range labels
        mySimpleXYPlot.setDomainLabel("Ora");
       	mySimpleXYPlot.setRangeLabel(s.getExtAddress()+" ("+s.getUnit()+")");
 
        // get rid of decimal points in our range labels:
       // mySimpleXYPlot.setRangeValueFormat(new DecimalFormat("0"));
 
        mySimpleXYPlot.setDomainValueFormat(new SimpleDateFormat("k:mm"));
 
        // by default, AndroidPlot displays developer guides to aid in laying out your plot.
        // To get rid of them call disableAllMarkup():
        mySimpleXYPlot.disableAllMarkup();
    }
    
    // What happens when you press the back button on your phone
    @Override
    public void onBackPressed() {
    	flipper.showPrevious();
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
    private class ListWithImageAdapter extends ArrayAdapter<Sensor> {

    	private ArrayList<Sensor> items;

    	public ListWithImageAdapter(Context context, int textViewResourceId, ArrayList<Sensor> items) {
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
                
                Sensor s = items.get(position);
                if (s != null) {
                        TextView t = (TextView) v.findViewById(R.id.txtName);
                        if (t != null) {
                              t.setText(s.getExtAddress());                            
                        }
                        t = (TextView) v.findViewById(R.id.txtLocation);
                        if (t != null) {
                              t.setText(s.getEndpoint());                            
                        }
                        /*t = (TextView) v.findViewById(R.id.txtValue);
                        if (t != null) {
                        		Uri sensorValue = Uri.parse(provider+"/value/"+s.getId());
                        		String[] columns = {"max(timestamp)","value"};
                        		Cursor c = managedQuery(sensorValue, columns, null, null, null);
                        		if (c.moveToFirst()) {
                        			t.setText(c.getDouble(c.getColumnIndex("value"))+((TemperatureSensor)s).getUnit());                            
                        		}
                        }
                        */
                        ImageView i = (ImageView) v.findViewById(R.id.img);
                        if (i != null) {
                        	if (s.getType()==SensorConstants.TEMPERATURE) {
                        		i.setImageResource(R.drawable.temperature);
                        	}
                        }
                }
                return v;
        }
    }
}