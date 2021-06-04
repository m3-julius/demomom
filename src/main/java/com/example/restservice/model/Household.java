package com.example.restservice.model;

import java.util.List;

public class Household {
	
	private final int houseid;
	private final String housetype;
	private final List<Person> members;
	
	public Household(int houseid, String housetype, List<Person> members) {
		this.houseid = houseid;
		this.housetype = housetype;
		this.members = members;
	}

	public int getHouseid() {
		return houseid;
	}

	public String getHousetype() {
		return housetype;
	}

	public List<Person> getMembers() {
		return members;
	}
	
}
