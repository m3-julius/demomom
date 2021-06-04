package com.example.restservice.model;

import java.util.Date;

public class Person {
	
	private final int personid;
	private final String name;
	private final String gender;
	private final String maritalid;
	private final int spouse;
	private final String occupationid;
	private final double annualincome;
	private final Date dob;
	
	public Person(int personid, String name, String gender, String maritalid, int spouse, String occupationid,
			double annualincome, Date dob) {
		this.personid = personid;
		this.name = name;
		this.gender = gender;
		this.maritalid = maritalid;
		this.spouse = spouse;
		this.occupationid = occupationid;
		this.annualincome = annualincome;
		this.dob = dob;
	}
	
	public int getPersonid() {
		return personid;
	}

	public String getName() {
		return name;
	}
	
	public String getGender() {
		return gender;
	}
	
	public String getMaritalid() {
		return maritalid;
	}
	
	public int getSpouse() {
		return spouse;
	}
	
	public String getOccupationid() {
		return occupationid;
	}
	
	public double getAnnualincome() {
		return annualincome;
	}
	
	public Date getDob() {
		return dob;
	}

}
