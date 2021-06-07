package com.example.restservice.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.validator.GenericValidator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.dao.MOMDAO;
import com.example.restservice.model.House;
import com.example.restservice.model.Household;

@RestController
public class HouseController {
	ApplicationContext context = 
    		new ClassPathXmlApplicationContext("Spring-Module.xml");
	MOMDAO momDAO = (MOMDAO) context.getBean("momDAO");
	
	private List<String> cfgHouseTypeIdList = momDAO.getValidHouseTypeIdList();
	
	@GetMapping("/createhouse")
	public House createhouse(@RequestParam(value = "housetype") String housetype) {
		if (GenericValidator.isBlankOrNull(housetype) || !isValidHouseType(housetype)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameter 'housetype' is empty or invalid (accepted input is H [HDB], C [Condo] or L [Landed]).");
		} else {
			try {
				int newhouseid = momDAO.insertHouse(housetype);
				
				if (newhouseid == -1) {
					throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred: House is not created.");
				}

				return new House(newhouseid, housetype);
			} catch (Exception e) {
				System.out.println(ExceptionUtils.getStackTrace(e));
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred: " + e.getMessage(), e);
			}
		}
		
	}
	
	@GetMapping("/deletehousehold")
	public String deletehousehold(@RequestParam(value = "houseid") String houseid) {
		if (GenericValidator.isBlankOrNull(houseid) || !StringUtils.isNumeric(houseid)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameter 'houseid' is empty or invalid (not numeric).");
		} else {
			try {
				int result = momDAO.deleteHousehold(Integer.parseInt(houseid));
				
				if (result <= 0) {
					return "Household houseid " + houseid + " is not deleted.";
				} else {
					return "Household houseid " + houseid + " and its members deleted.";
				}
			} catch (Exception e) {
				System.out.println(ExceptionUtils.getStackTrace(e));
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred: " + e.getMessage(), e);
			}
		}
	}
	
//	@GetMapping("/listallhouseholds")
//	public List<Household> listhouseholds() {
//		try {
//			return momDAO.retrieveHouseholdData("all");
//		} catch (Exception e) {
//			System.out.println(ExceptionUtils.getStackTrace(e));
//			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred: " + e.getMessage(), e);
//		}
//	}
	
	@GetMapping("/gethousehold")
	public List<Household> gethousehold(@RequestParam(value = "houseid") String houseid) {
		String validationerror = validateHouseholdInput(houseid);
		if (!StringUtils.isBlank(validationerror)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, validationerror);
		}
		
		try {
			return momDAO.retrieveHouseholdData(houseid);
		} catch (Exception e) {
			System.out.println(ExceptionUtils.getStackTrace(e));
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred: " + e.getMessage(), e);
		}
	}
	
	private String validateHouseholdInput(String houseid) {
		String error = "";
		
		if (GenericValidator.isBlankOrNull(houseid)) {
			error += "Parameter 'houseid' is empty. ";
		} else {
			if (!houseid.equalsIgnoreCase("all") && !StringUtils.isNumeric(houseid)) {
				error += "Parameter 'houseid' must be 'all' or a valid houseid numeric value. ";
			} else if (StringUtils.isNumeric(houseid) && !momDAO.isHouseIdExists(Integer.parseInt(houseid))) {
				error += "houseid " + houseid + " does not exists. ";
			}
		}
		
		return error;
	}

	
	private boolean isValidHouseType(String housetype) {
		return cfgHouseTypeIdList.contains(housetype);
	}

	public List<String> getCfgHouseTypeIdList() {
		return cfgHouseTypeIdList;
	}

	public void setCfgHouseTypeIdList(List<String> cfgHouseTypeIdList) {
		this.cfgHouseTypeIdList = cfgHouseTypeIdList;
	}
	
	
}
