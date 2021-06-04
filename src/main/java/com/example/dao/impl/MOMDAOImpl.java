package com.example.dao.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.example.dao.MOMDAO;
import com.example.restservice.model.Household;
import com.example.restservice.model.HouseholdMember;
import com.example.restservice.model.HouseholdNoSpouse;
import com.example.restservice.model.Person;
import com.example.restservice.model.PersonNoSpouse;

public class MOMDAOImpl implements MOMDAO {
	
	private Connection getConnection(boolean autocommit) {
		String connectionUrl = "jdbc:mysql://localhost:3306/momdb";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(connectionUrl, "momtest","momtest");
			conn.setAutoCommit(autocommit);
		} catch (SQLException e) {
			System.out.println("Error in getting SQL Connection! Message: " + e.getMessage());
        	throw new RuntimeException(e);
		}
		
		return conn;
	}
	
	private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
            conn.close();
            } catch (SQLException e) {
            	throw new RuntimeException(e);
            }
        }
	}
	
	@Override
    public void getTestTable() {
        String sql = "SELECT * FROM CUSTOMER";
        Connection conn = getConnection(true);
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
//            ps.setInt(1, custId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
            	System.out.println("{" + rs.getInt("CUST_ID") + ", " + 
            			rs.getString("NAME") + ", " + rs.getInt("AGE") + "}");
            }
            
            rs.close();
            ps.close();
        } catch (SQLException e) {
			System.out.println("Error in getTestTable(): " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }
    }

	@Override
	public int insertHouse(String housetype) {
		int createdId = -1;
		
        String sql = "insert into HOUSE (housetypeid) VALUES (?)";
        Connection conn = getConnection(true);
        PreparedStatement ps;
		try {
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	        ps.setString(1, housetype);
	        int affectedRows = ps.executeUpdate();
	        
	        if (affectedRows == 0) {
	        	System.out.println("Inserting HOUSE failed, no rows affected.");
	        } else {
		        ResultSet rs = ps.getGeneratedKeys();
		        if (rs.next()) {
		        	createdId = rs.getInt(1);
		        }
	        	
		        rs.close();
	        }
	        
	        ps.close();
		} catch (SQLException e) {
			System.out.println("Error in insertHouse(): " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }
		
		return createdId;
	}
	
	@Override
	public int insertPerson(Connection conn, String name, String gender, String maritalid,
			String spouse, String occupationid, double annualincome, Date dob) {
		
		int createdId = -1;
		
        String sql = "insert into PERSON (name, gender, maritalid, spouse, occupationid,"
        		+ "annualincome, dob) VALUES (?,?,?,?,?,?,?)";
        
        PreparedStatement ps;
		try {
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	        ps.setString(1, name);
	        ps.setString(2, gender);
	        ps.setString(3, maritalid);
	        ps.setString(4, (StringUtils.isBlank(spouse) ? null : spouse));
	        ps.setString(5, occupationid);
	        ps.setDouble(6, annualincome);
	        ps.setTimestamp(7, new Timestamp(dob.getTime()));
	        int affectedRows = ps.executeUpdate();

	        if (affectedRows == 0) {
	        	System.out.println("Inserting PERSON failed, no rows affected.");
	        } else {
		        ResultSet rs = ps.getGeneratedKeys();
		        if (rs.next()) {
		        	createdId = rs.getInt(1);
		        }
		        
		        rs.close();
	        }
	        
	        ps.close();
		} catch (SQLException e) {
			System.out.println("Error in insertHouse(): " + e.getMessage());
        }
		
		return createdId;
	}
		
	@Override
	public int insertHouseholdMember(int houseid, String name, String gender, String maritalid,
			String spouse, String occupationid, double annualincome, Date dob) {
		
        Connection conn = getConnection(false);
		
		int personid = insertPerson(conn, name, gender, maritalid, spouse, occupationid, annualincome, dob);

		String sql = "insert into HOUSEHOLD (houseid, personid) VALUES (?,?)";
        PreparedStatement ps;
        
		try {
			if (personid == -1) {
				System.out.println("Error in insertHouseholdMember(): Failed to insert PERSON.");
				conn.rollback();
			} else {
				ps = conn.prepareStatement(sql);
		        ps.setInt(1, houseid);
		        ps.setInt(2, personid);
		        int affectedRows = ps.executeUpdate();
		        
		        if (affectedRows == 0) {
		        	System.out.println("Inserting HOUSEHOLD failed, no rows affected.");
		        	personid = -1;
		        	conn.rollback();
		        } else {
			        conn.commit();
		        }
		        
		        ps.close();
			}
		} catch (SQLException e) {
			System.out.println("Error in insertHouseholdMember(): " + e.getMessage());
			try {
				conn.rollback();
			} catch (SQLException e1) {
	            throw new RuntimeException(e1);
			}
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }
		
		return personid;
	}
	
	@Override
	public List<String> getValidMaritalIdList() {
		List<String> maritalIdList = new ArrayList<String>();
		
        String sql = "SELECT MARITALID FROM CFG_MARITAL_STATUS";
        Connection conn = getConnection(true);
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next())
            	maritalIdList.add(rs.getString("maritalid"));
            
            rs.close();
            ps.close();
        } catch (SQLException e) {
			System.out.println("Error in isValidMaritalId(): " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }
        
        return maritalIdList;
	}
    
	@Override
	public List<String> getValidHouseTypeIdList() {
		List<String> houseTypeIdList = new ArrayList<String>();
		
        String sql = "SELECT HOUSETYPEID FROM CFG_HOUSE_TYPE";
        Connection conn = getConnection(true);
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next())
            	houseTypeIdList.add(rs.getString("housetypeid"));
            
            rs.close();
            ps.close();
        } catch (SQLException e) {
			System.out.println("Error in getValidHouseTypeIdList(): " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }
        
        return houseTypeIdList;
	}
	
	@Override
	public List<String> getValidOccupationIdList() {
		List<String> occupationIdList = new ArrayList<String>();
		
        String sql = "SELECT OCCUPATIONID FROM CFG_OCCUPATION";
        Connection conn = getConnection(true);
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next())
            	occupationIdList.add(rs.getString("occupationid"));
            
            rs.close();
            ps.close();
        } catch (SQLException e) {
			System.out.println("Error in getValidOccupationIdList(): " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }
        
        return occupationIdList;
	}
	
	@Override
	public List<Household> retrieveHouseholdData(String houseid) {
		List<Household> householdList = null;

		List<HouseholdMember> housememberList = getHouseholdMembers(houseid);
		if (housememberList != null) {
			householdList = new ArrayList<Household>();
			
			int uniquehouseid = -1;
			List<Person> personList = new ArrayList<Person>();
			for (int i=0; i<housememberList.size(); i++) {
				HouseholdMember member = housememberList.get(i);
				
				if (i == 0) {
					uniquehouseid = member.getHouseid();
				} else if (uniquehouseid != member.getHouseid()) {
					householdList.add(new Household(uniquehouseid, personList));
					
					uniquehouseid = member.getHouseid();
					personList = new ArrayList<Person>();
				}
				
				personList.add(getPerson(member.getPersonid()));
				if (i == housememberList.size()-1) {
					householdList.add(new Household(uniquehouseid, personList));
				}
			}
		}
		
		return householdList;
	}
	
	@Override
	public List<HouseholdNoSpouse> retrieveHouseholdNoSpouse(String houseid) {
		List<HouseholdNoSpouse> householdList = null;

		List<HouseholdMember> housememberList = getHouseholdMembers(houseid);
		if (housememberList != null) {
			householdList = new ArrayList<HouseholdNoSpouse>();
			
			List<PersonNoSpouse> personList = new ArrayList<PersonNoSpouse>();
			for (HouseholdMember member : housememberList) {
				personList.add(getPersonNoSpouse(member.getPersonid()));
			}
			
			householdList.add(new HouseholdNoSpouse(housememberList.get(0).getHouseid(), personList));
		}
		
		return householdList;
	}
	
	private List<HouseholdMember> getHouseholdMembers(String houseid) {
		List<HouseholdMember> ret = null;
		
		String sql = "SELECT * FROM HOUSEHOLD ";
		if (!houseid.equalsIgnoreCase("all"))
			sql += "WHERE HOUSEID=? ";
		sql += "ORDER BY HOUSEID ASC";

        Connection conn = getConnection(true);
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            if (!houseid.equalsIgnoreCase("all")) {
                ps.setString(1, houseid);
            }
            ResultSet rs = ps.executeQuery();
            
            if (!rs.next()) {
            	ret = null;
            } else {
            	ret = new ArrayList<HouseholdMember>();
            	do {
            		ret.add( new HouseholdMember(rs.getInt("houseid"), rs.getInt("personid")) );
            	} while (rs.next());
            }
            
            rs.close();
            ps.close();
        } catch (SQLException e) {
			System.out.println("Error in getHouseholdMembers(): " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }		
		
		return ret;
	}
	
	private Person getPerson(int personid) {
		Person ret = null;
		
        String sql = "SELECT * FROM PERSON WHERE PERSONID=?";
        Connection conn = getConnection(true);
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, personid);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next())
            	ret = new Person(rs.getString("name"), rs.getString("gender"), 
            			rs.getString("maritalid"), rs.getInt("spouse"), rs.getString("occupationid"),
            			rs.getDouble("annualincome"), new Date(rs.getTimestamp("dob").getTime()));
            	
            rs.close();
            ps.close();
        } catch (SQLException e) {
			System.out.println("Error in getPerson(): " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }		
		
		return ret;
	}

	private PersonNoSpouse getPersonNoSpouse(int personid) {
		PersonNoSpouse ret = null;
		
        String sql = "SELECT * FROM PERSON WHERE PERSONID=?";
        Connection conn = getConnection(true);
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, personid);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next())
            	ret = new PersonNoSpouse(rs.getString("name"), rs.getString("gender"), 
            			rs.getString("maritalid"), rs.getString("occupationid"),
            			rs.getDouble("annualincome"), new Date(rs.getTimestamp("dob").getTime()));
            	
            rs.close();
            ps.close();
        } catch (SQLException e) {
			System.out.println("Error in getPerson(): " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }		
		
		return ret;
	}

	@Override
	public boolean isHouseIdExists(int houseid) {
		boolean ret = false;
		
        String sql = "SELECT * FROM HOUSE WHERE HOUSEID=?";
        Connection conn = getConnection(true);
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, houseid);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next())
            	ret = true;
            
            rs.close();
            ps.close();
        } catch (SQLException e) {
			System.out.println("Error in isHouseIdExists(): " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }		
		
		return ret;
	}
	
	@Override
	public boolean isPersonIdExists(int personid) {
		boolean ret = false;
		
        String sql = "SELECT * FROM PERSON WHERE PERSONID=?";
        Connection conn = getConnection(true);
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, personid);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next())
            	ret = true;
            
            rs.close();
            ps.close();
        } catch (SQLException e) {
			System.out.println("Error in isPersonIdExists(): " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }		
		
		return ret;
	}

	
}
