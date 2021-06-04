package com.example.restservice.model;

import java.util.List;

public class Household {
	
	private final int houseid;
	private final List<Person> members;
	
	public Household(int houseid, List<Person> members) {
		this.houseid = houseid;
		this.members = members;
	}

	public int getHouseid() {
		return houseid;
	}

	public List<Person> getMembers() {
		return members;
	}
	
}
