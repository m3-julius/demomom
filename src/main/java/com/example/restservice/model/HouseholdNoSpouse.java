package com.example.restservice.model;

import java.util.List;

public class HouseholdNoSpouse {
	
	private final int houseid;
	private final List<PersonNoSpouse> members;
	
	public HouseholdNoSpouse(int houseid, List<PersonNoSpouse> members) {
		this.houseid = houseid;
		this.members = members;
	}

	public int getHouseid() {
		return houseid;
	}

	public List<PersonNoSpouse> getMembers() {
		return members;
	}
	
}
