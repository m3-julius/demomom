package com.example.restservice.model;

public class House {
	
	private final long houseid;
	private final String housetypeid;

	public House(long houseid, String housetypeid) {
		this.houseid = houseid;
		this.housetypeid = housetypeid;
	}

	public long getHouseid() {
		return houseid;
	}

	public String getHousetypeid() {
		return housetypeid;
	}

}
