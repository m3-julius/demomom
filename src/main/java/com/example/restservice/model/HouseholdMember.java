package com.example.restservice.model;

public class HouseholdMember {
	
	private final int houseid;
	private final int personid;
	
	public HouseholdMember(int houseid, int personid) {
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
