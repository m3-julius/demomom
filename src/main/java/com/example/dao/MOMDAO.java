package com.example.dao;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import com.example.restservice.model.Household;
import com.example.restservice.model.HouseholdNoSpouse;

public interface MOMDAO {
	
	public void getTestTable();
	
	public int insertHouse(String housetype);
	
	public int insertPerson(Connection conn, String name, String gender, String maritalid,
			String spouse, String occupationid, double annualincome, Date dob);
	
	public int insertHouseholdMember(int houseid, String name, String gender, String maritalid,
			String spouse, String occupationid, double annualincome, Date dob);
	
	public List<String> getValidMaritalIdList();
	
	public List<String> getValidHouseTypeIdList();
	
	public List<String> getValidOccupationIdList();
	
	public List<Household> retrieveHouseholdData(String houseid);
	
	public List<HouseholdNoSpouse> retrieveHouseholdNoSpouse(String houseid);
	
	public boolean isHouseIdExists(int houseid);
	
	public boolean isPersonIdExists(int personid);
	
}
