package com.example.restservice.model;

public class Household {
	
	private final int houseid;
	private final int personid;
	
	public Household(int houseid, int personid) {
		this.houseid = houseid;
		this.personid = personid;
	}

	public int getHouseid() {
		return houseid;
	}

	public int getPersonid() {
		return personid;
	}
	
}
