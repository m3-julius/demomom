package com.example.restservice.model;

import java.util.List;

public class HouseholdNoSpouse {
	
	private final String housetype;
	private final List<PersonNoSpouse> members;
	
	public HouseholdNoSpouse(String housetype, List<PersonNoSpouse> members) {
		this.housetype = housetype;
		this.members = members;
	}

	public String getHousetype() {
		return housetype;
	}

	public List<PersonNoSpouse> getMembers() {
		return members;
	}
	
}
