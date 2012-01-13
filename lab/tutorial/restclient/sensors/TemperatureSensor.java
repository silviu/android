package lab.tutorial.restclient.sensors;


public class TemperatureSensor extends Sensor {
	
	private String unit = "°C";
	
	public TemperatureSensor(int id, String extAddress, String endpoint, String clusterID, String location) {
		super(id, extAddress, endpoint, clusterID, location, SensorConstants.TEMPERATURE);
	}
	
	public String getUnit() {
		return unit;
	}

}
