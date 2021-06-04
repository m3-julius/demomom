package com.example.restservice.model;

public class House {
	
	private final int houseid;
	private final String housetypeid;

	public House(int houseid, String housetypeid) {
		this.houseid = houseid;
		this.housetypeid = housetypeid;
	}

	public int getHouseid() {
		return houseid;
	}

	public String getHousetypeid() {
		return housetypeid;
	}

}
