package org.fogbowcloud.blowout.infrastructure.token;

public enum UpdateTimeUnitsEnum {

	HOUR("H"), MINUTES("M"), SECONDS("S"), MILLISECONDS("MS");
	
	private String value;
	
	private UpdateTimeUnitsEnum(String value){
		this.value = value;
	}
	
	public String getValue(){
		return this.value;
	}
}
