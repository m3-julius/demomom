package com.example.restservice.controller;

import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.dao.MOMDAO;
import com.example.restservice.model.House;
import com.example.restservice.model.Household;

@RestController
public class GrantController {
	ApplicationContext context = 
    		new ClassPathXmlApplicationContext("Spring-Module.xml");
	MOMDAO momDAO = (MOMDAO) context.getBean("momDAO");
	
	@GetMapping("/liststudentgrant")
	public List<Household> liststudentgrant() {
		try {
			return momDAO.retrieveGrantStudentEncBonus();
		} catch (Exception e) {
//			System.out.println(ExceptionUtils.getStackTrace(e));
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred: " + e.getMessage(), e);
		}
	}
	
	@GetMapping("/listfamilygrant")
	public List<Household> listfamilygrant() {
		try {
			return momDAO.retrieveGrantFamilyScheme();
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred: " + e.getMessage(), e);
		}
	}
	
	@GetMapping("/listeldergrant")
	public List<Household> listeldergrant() {
		try {
			return momDAO.retrieveGrantElderBonus();
			
		} catch (Exception e) {
			System.out.println(ExceptionUtils.getStackTrace(e));
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred: " + e.getMessage(), e);
		}
	}
	
	@GetMapping("/listbabygrant")
	public List<Household> listbabygrant() {
		try {
			return momDAO.retrieveGrantBabySunshine();
			
		} catch (Exception e) {
			System.out.println(ExceptionUtils.getStackTrace(e));
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred: " + e.getMessage(), e);
		}
	}
	
	@GetMapping("/listyologrant")
	public List<House> listyologrant() {
		try {
			return momDAO.retrieveGrantYOLO();
			
		} catch (Exception e) {
			System.out.println(ExceptionUtils.getStackTrace(e));
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred: " + e.getMessage(), e);
		}
	}
	
	
}
