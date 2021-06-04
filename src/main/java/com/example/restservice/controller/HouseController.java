package com.example.restservice.controller;

import java.util.List;

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
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred: " + e.getMessage(), e);
			}
		}
		
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
