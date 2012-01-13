package lab.tutorial.restclient.sensors;


public class TemperatureSensor extends Sensor {
	
	private String unit = "°C";
	
	public TemperatureSensor(int id, String extAddress, String endpoint, String clusterID, Long timestamp) {
		super(id, extAddress, endpoint, clusterID, timestamp, SensorConstants.TEMPERATURE);
	}
	
	public String getUnit() {
		return unit;
	}

}
