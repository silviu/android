package lab.tutorial.restclient.sensors;

public class FlowSensor extends Sensor {
	
	private String unit = "m³/h";
	
	public FlowSensor(int id, String extAddress, String endpoint, String clusterID, String location, Long timestamp) {
		super(id, extAddress, endpoint, clusterID, location, SensorConstants.FLOW, timestamp);
	}
	
	public String getUnit() {
		return unit;
	}

}
