// This class describes a general sensor

package lab.tutorial.restclient.sensors;


abstract public class Sensor {
		private int id;
		private String extAddress;
		private String endpoint;
		private String clusterID;
		String location;
		Long timestamp;
		private String type;
		
		public Sensor(int id, String extAddress, String endpoint, String clusterID, String location, String type, Long timestamp) {
			this.id = id;
			this.setExtAddress(extAddress);
			this.setEndpoint(endpoint);
			this.setClusterID(clusterID);
			this.setLocation(location);
			this.setTimestamp(timestamp);
			this.type = type;
		}
		
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
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
		
		public void setLocation(String location) {
			this.location = location;
		}

		public String getLocation() {
			return location;
		}
		public void setTimestamp(Long timestamp) {
			this.timestamp = timestamp;
		}

		public Long getTimestamp() {
			return timestamp;
		}


		
}
