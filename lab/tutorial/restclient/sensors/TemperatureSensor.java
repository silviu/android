package lab.tutorial.restclient.sensors;


public class TemperatureSensor extends Sensor {
	
	private String unit = "°C";
	
	public TemperatureSensor(int id, String extAddress, String endpoint, String clusterID) {
		super(id, extAddress, endpoint, clusterID, SensorConstants.TEMPERATURE);
	}
	
	public String getUnit() {
		return unit;
	}

}
