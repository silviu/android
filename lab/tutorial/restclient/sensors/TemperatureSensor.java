package lab.tutorial.restclient.sensors;

public class TemperatureSensor extends Sensor {
	
	private String unit = "°C";
	
	public TemperatureSensor(int id, String extAddress, String endpoint, String clusterID, String location, Long timestamp) {
		super(id, extAddress, endpoint, clusterID, location, SensorConstants.TEMPERATURE, timestamp);
	}
	
	public String getUnit() {
		return unit;
	}

}
