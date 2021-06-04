package com.example.restservice.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.validator.GenericValidator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.dao.MOMDAO;
import com.example.restservice.model.Household;

@RestController
public class PersonController {
	private final String GENDER_MALE = "M";
	private final String GENDER_FEMALE = "F";
	private final String MARITAL_ID_MARRIED = "M";
	private final String OCCUPATION_ID_EMPLOYED = "EM";
	private final String DATE_PATTERN_DDMMYYYY = "ddMMyyyy";
	
	ApplicationContext context = 
    		new ClassPathXmlApplicationContext("Spring-Module.xml");
	MOMDAO momDAO = (MOMDAO) context.getBean("momDAO");
	
	private List<String> cfgMaritalIdList = momDAO.getValidMaritalIdList();
	private List<String> cfgOccupationIdList = momDAO.getValidOccupationIdList();
	
	@GetMapping("/createhouseholdmember")
	public Household inserthousemember(
			@RequestParam(value = "houseid") String houseid,
			@RequestParam(value = "name") String name,
			@RequestParam(value = "gender") String gender,
			@RequestParam(value = "maritalid") String maritalid,
			@RequestParam(value = "spouse", required = false) String spouse,
			@RequestParam(value = "occupationid") String occupationid,
			@RequestParam(value = "annualincome", required = false, defaultValue = "0.00") String annualincome,
			@RequestParam(value = "dob") String dob
			) 
	{
		String validationerror = validateHousememberInputs(houseid, name, gender, maritalid, spouse, occupationid, annualincome, dob);
		if (!validationerror.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, validationerror);
		}

		try {
			int houseidint = Integer.parseInt(houseid);
			SimpleDateFormat sdfrmt = new SimpleDateFormat(DATE_PATTERN_DDMMYYYY);
			Date dobDate = sdfrmt.parse(dob);
			
			int newpersonid = momDAO.insertHouseholdMember(houseidint, name, gender, maritalid, spouse, occupationid, Double.parseDouble(annualincome), dobDate);
			
			if (newpersonid == -1) {
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred: Household member is not created.");
			}
			
			return new Household(houseidint, newpersonid);
			
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred: " + e.getMessage(), e);
		}

	}
	
	private String validateHousememberInputs(String houseid, String name, String gender, String maritalid, String spouse, String occupationid, String annualincome, String dob ) {
		String error = "";
		
		if (GenericValidator.isBlankOrNull(houseid) || !StringUtils.isNumeric(houseid)) {
			error += "Parameter 'houseid' is empty or invalid (not numeric). ";
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameter 'houseid' is empty or invalid (not numeric).");
		}
		
		if (!momDAO.isHouseIdExists(Integer.parseInt(houseid))) {
			error += "houseid " + houseid + " does not exists.";
		}
		
		if (GenericValidator.isBlankOrNull(name) || !StringUtils.isAlphanumericSpace(name)) {
			error += "Parameter 'name' is empty or invalid (not alphanumeric). ";
		}
		
		if (GenericValidator.isBlankOrNull(gender) || !isValidGender(gender)) {
			error += "Parameter 'gender' is empty or invalid (accepted input is M [Male] or F [Female]). ";
		}
		
		if (GenericValidator.isBlankOrNull(maritalid) || !isValidMaritalId(maritalid)) {
			error += "Parameter 'maritalid' is empty or invalid (accepted input is S [Single] or M [Married]). ";
		}
		
		if (maritalid.equals(MARITAL_ID_MARRIED) && GenericValidator.isBlankOrNull(spouse)) {
			error += "Parameter 'spouse' is required as the maritalid is Married. ";
		}
		
		if (!GenericValidator.isBlankOrNull(spouse)) {
			if (!StringUtils.isNumeric(spouse))
				error += "Parameter 'spouse' is not numeric. ";
			else if (!momDAO.isPersonIdExists(Integer.parseInt(spouse)))
				error += "'spouse' " + spouse + " does not exists. ";
		}
		
		if (GenericValidator.isBlankOrNull(occupationid) || !isValidOccupationId(occupationid)) {
			error += "Parameter 'occupationid' is empty or invalid (accepted input is ST [Student], UN [Unemployed] or EM [Employed]). ";
		}
		
		if (!GenericValidator.isBlankOrNull(annualincome) && !isValidAnnualIncome(occupationid, annualincome)) {
			error += "Parameter 'annualincome' is not a valid decimal value or not a positive decimal number. ";
		}
		
		if (GenericValidator.isBlankOrNull(dob) || !isDatePatternValid(dob)) {
			error += "Parameter 'dob' is empty or invalid (accepted date pattern is ddmmyyyy. ";
		}
		
		return error;
	}

	private boolean isValidGender(String gender) {
		boolean ret = false;
		
		if (gender.equals(GENDER_MALE) || gender.equals(GENDER_FEMALE)) {
			ret = true;
		} else {
			ret = false;
		}
		
		return ret;
	}
	
	private boolean isValidMaritalId(String maritalid) {
		return cfgMaritalIdList.contains(maritalid);
	}
	
	private boolean isValidOccupationId(String occupationid) {
		return cfgOccupationIdList.contains(occupationid);
	}

	private boolean isDatePatternValid(String strDate) {
		SimpleDateFormat sdfrmt = new SimpleDateFormat(DATE_PATTERN_DDMMYYYY);
		sdfrmt.setLenient(false);

		try {
			sdfrmt.parse(strDate); 
		} catch (ParseException e) {
			return false;
		}

		return true;
	}
	
	private boolean isValidAnnualIncome(String occupationid, String annualincome) {
		if (!GenericValidator.isDouble(annualincome)) {
			return false;
		} else {
			double annualinc = Double.parseDouble(annualincome);
			if (annualinc < NumberUtils.DOUBLE_ZERO) {
				return false;
			}
		}
		
		return true;
	}
	
	public List<String> getCfgMaritalIdList() {
		return cfgMaritalIdList;
	}

	public void setCfgMaritalIdList(List<String> cfgMaritalIdList) {
		this.cfgMaritalIdList = cfgMaritalIdList;
	}

	public List<String> getCfgOccupationIdList() {
		return cfgOccupationIdList;
	}

	public void setCfgOccupationIdList(List<String> cfgOccupationIdList) {
		this.cfgOccupationIdList = cfgOccupationIdList;
	}
	
}
