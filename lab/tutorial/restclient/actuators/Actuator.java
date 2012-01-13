// This class describes a general actuator

package lab.tutorial.restclient.actuators;

abstract public class Actuator {
		private int id;
		private String extAddress;
		private String endpoint;
		private String clusterID;
		private Long timestamp;
		private String location;
		private String setting;
		private String type;
		
		public Actuator(int id, String extAddress, String endpoint, String clusterID, String location, Long timestamp ,String setting, String type) {
			this.id = id;
			this.setExtAddress(extAddress);
			this.setEndpoint(endpoint);
			this.setClusterID(clusterID);
			this.setTimestamp(timestamp);
			this.setLocation(location);
			this.setSetting(setting);
			this.type = type;
		}
		
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}

		public void setExtAddress(String extAddress) {
			this.extAddress = extAddress;
		}

		public String getExtAddress() {
			return extAddress;
		}

		public void setEndpoint(String endpoint) {
			this.endpoint = endpoint;
		}

		public String getEndpoint() {
			return endpoint;
		}

		public void setClusterID(String clusterID) {
			this.clusterID = clusterID;
		}

		public String getClusterID() {
			return clusterID;
		}

		public void setTimestamp(Long timestamp) {
			this.timestamp = timestamp;
		}

		public Long getTimestamp() {
			return timestamp;
		}

		public void setSetting(String setting) {
			this.setting = setting;
		}

		public String getSetting() {
			return setting;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}	
		public void setLocation(String location) {
			this.location = location;
		}

		public String getLocation() {
			return location;
		}
}
