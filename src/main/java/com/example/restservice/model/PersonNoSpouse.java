package com.example.restservice.model;

import java.util.Date;

public class PersonNoSpouse {
	
	private final String name;
	private final String gender;
	private final String maritalid;
	private final String occupationid;
	private final double annualincome;
	private final Date dob;
	
	public PersonNoSpouse(String name, String gender, String maritalid, String occupationid,
			double annualincome, Date dob) {
		this.name = name;
		this.gender = gender;
		this.maritalid = maritalid;
		this.occupationid = occupationid;
		this.annualincome = annualincome;
		this.dob = dob;
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
