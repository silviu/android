// This class describes an On/Off Switch actuator

package lab.tutorial.restclient.actuators;


public class OnOffSwitch extends Actuator {
	
	private Boolean setting;
	public String onValue = "on";
	public String offValue = "off";
	
	public OnOffSwitch(int id, String extAddress, String endpoint, String clusterID, String location, Long timestamp, String setting) {
		super(id, extAddress, endpoint, clusterID,location, timestamp, setting, ActuatorConstants.ONOFF);
		if (setting.equalsIgnoreCase(onValue)) this.setting=true;
		else this.setting = false;
	}
	
	public Boolean getSwitchSetting(){
		return this.setting;
	}
	
	public void setSwitchSetting(String setting) {
		if (setting.equalsIgnoreCase(onValue)) this.setting = true;
		else this.setting = false;
	}
	
	public void setSwitchSetting(Boolean setting){
		this.setting = setting;
	}

}
