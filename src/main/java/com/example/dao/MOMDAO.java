package com.example.dao;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import com.example.restservice.model.House;
import com.example.restservice.model.Household;
import com.example.restservice.model.HouseholdNoSpouse;

public interface MOMDAO {
	
	public int insertHouse(String housetype);
	
	public int insertPerson(Connection conn, String name, String gender, String maritalid,
			String spouse, String occupationid, double annualincome, Date dob, int houseid);
	
	public int deleteHousehold(int houseid);

	public int insertHouseholdMember(int houseid, String name, String gender, String maritalid,
			String spouse, String occupationid, double annualincome, Date dob);

	public int deleteHouseholdMember(int personid);
	
	public List<String> getValidMaritalIdList();
	
	public List<String> getValidHouseTypeIdList();
	
	public List<String> getValidOccupationIdList();
	
	public List<Household> retrieveHouseholdData(String houseid);
	
	public List<HouseholdNoSpouse> retrieveHouseholdNoSpouse(String houseid);
	
	public List<Household> retrieveGrantStudentEncBonus();
	
	public List<Household> retrieveGrantFamilyScheme();
	
	public List<Household> retrieveGrantElderBonus();
	
	public List<Household> retrieveGrantBabySunshine();
	
	public List<House> retrieveGrantYOLO();
	
	public boolean isHouseIdExists(int houseid);
	
	public boolean isPersonIdExists(int personid);
	
}
