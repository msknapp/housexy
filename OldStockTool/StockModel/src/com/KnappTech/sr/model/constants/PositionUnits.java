package com.KnappTech.sr.model.constants;

public enum PositionUnits {
	DOLLARS,
	CENTS,
	CONTRACTS;
	
	public String deflate() {
		PositionUnits[] options = PositionUnits.values();
		for (int i = 0;i<options.length;i++){
			if (options[i] == this) {
				return String.valueOf(i);
			}
		}
		return "";
	}
	
	public static PositionUnits inflate(String portion) {
		PositionUnits[] options = PositionUnits.values();
		int i = Integer.parseInt(portion);
		return options[i];
	}
}
