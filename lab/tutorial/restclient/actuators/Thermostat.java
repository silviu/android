// This class describes an Termostat Switch actuator
package lab.tutorial.restclient.actuators;

import android.util.Log;

public class Thermostat extends Actuator {
	
	private double minVal;
	private double maxVal;
	
	public Thermostat(int id, String extAddress, String endpoint, String clusterID, String location, Long timestamp, String setting) {
		super(id, extAddress, endpoint, clusterID,location, timestamp, setting, ActuatorConstants.THERMOSTAT);
		setMinMaxVal(setting);
	}
	
	private void setMinMaxVal(String setting)
	{
		String min = setting.split("#")[0];
		String max = setting.split("#")[1];
		setminVal(Double.parseDouble(min));
		setminVal(Double.parseDouble(max));
	}
	
	public double getminVal(){
		return this.minVal;
	}
	
	public void setminVal(double minVal) {
		this.minVal = minVal;
	}
	
	public double getmaxVal(){
		return this.maxVal;
	}
	
	public void setmaxVal(double maxVal) {
		this.maxVal = maxVal;
	}
}
