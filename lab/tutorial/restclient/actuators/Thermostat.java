// This class describes an Termostat Switch actuator
package lab.tutorial.restclient.actuators;

public class Thermostat extends Actuator {
	
	private double minVal;
	private double maxVal;
	
	public Thermostat(int id, String extAddress, String endpoint, String clusterID, String location, Long timestamp, String setting) {
		super(id, extAddress, endpoint, clusterID,location, timestamp, setting, ActuatorConstants.THERMOSTAT);
		setMinMaxVal(setting);
	}
	
	private void setMinMaxVal(String setting)
	{
		//[{0015:6eb},{0016:cb}]
		String common = setting.substring(2, setting.length()-2);
		String min_partial = common.split(",")[0].split(":")[1];
		String min_str = min_partial.substring(0, min_partial.length()-1);
		double min = ((double)Integer.parseInt(min_str, 16))/100;
		setminVal(min);
		
		String max_partial = common.split(",")[1];
		String max_str = max_partial.split(":")[1];
		double max = ((double)Integer.parseInt(max_str, 16))/100;
		setmaxVal(max);
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
