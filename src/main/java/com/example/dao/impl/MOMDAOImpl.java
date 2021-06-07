package com.example.dao.impl;

import java.io.IOException;
import java.io.InputStream;
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
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.example.constants.MOMConstants;
import com.example.dao.MOMDAO;
import com.example.restservice.model.House;
import com.example.restservice.model.Household;
import com.example.restservice.model.HouseholdMap;
import com.example.restservice.model.Person;

public class MOMDAOImpl implements MOMDAO {
	
	private Connection getConnection(boolean autocommit) {
		
        Properties prop = new Properties();
        try (InputStream input = MOMDAOImpl.class.getClassLoader().getResourceAsStream("jdbc.properties")) {

            if (input == null) {
                System.out.println("Unable to find jdbc.properties");
                return null;
            }

            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
		
		String connectionUrl = prop.getProperty("jdbc.momdb.url");
		String username = prop.getProperty("jdbc.momdb.username");
		String password = prop.getProperty("jdbc.momdb.password");
		
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(connectionUrl, username, password);
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
			String spouse, String occupationid, double annualincome, Date dob, int houseid) {
		
		int createdId = -1;
		
        String sql = "insert into PERSON (name, gender, maritalid, spouse, occupationid,"
        		+ "annualincome, dob) VALUES (?,?,?,?,?,?,?)";
        
        int affectedRows = 0;
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
	        affectedRows = ps.executeUpdate();

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
			System.out.println("Error in insertPerson(): " + e.getMessage());
        }
		
		int updateRows = 0;
		if (!StringUtils.isBlank(spouse) && affectedRows > 0) {
			if (isValidSpouse(Integer.parseInt(spouse))) {
				updateRows = updatePerson(conn, Integer.parseInt(spouse), createdId);
				if (updateRows == 0)
					createdId = -2;
			} else {
				createdId = -2;
			}
		}
		
		return createdId;
	}
	
	@Override
	public int deleteHousehold(int houseid) {
        int result = 0;

		if (!isHouseIdExists(houseid)) {
			return result;
		}

		List<HouseholdMap> members = getHouseholdMembers(Integer.toString(houseid));
		
        String sql = "delete from household where houseid = ?";
        Connection conn = getConnection(false);
        PreparedStatement ps;
		try {
			ps = conn.prepareStatement(sql);
	        ps.setInt(1, houseid);
	        result = ps.executeUpdate();
	        
	        ps.close();
	        
	        // Delete the related records from HOUSE and PERSON table
        	int deletedHouseRows = deleteHouse(conn, houseid);
	        if (deletedHouseRows > 0) {
		        if (members != null && !members.isEmpty()) {
		        	for (HouseholdMap m : members) {
		        		int deletedPersonRows = deletePerson(conn, m.getPersonid());
		        		if (deletedPersonRows <= 0) {
		        			result = 0;
		        			break;
		        		}
		        	}
		        }
		        result = deletedHouseRows;
	        } else {
	        	result = 0;
	        }
	        
	        if (result > 0)
	        	conn.commit();
	        else
	        	conn.rollback();
		} catch (SQLException e) {
			System.out.println("Error in deleteHousehold(): " + e.getMessage());
			try {
				conn.rollback();
			} catch (SQLException e1) {
	            throw new RuntimeException(e1);
			}
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }

		return result;
	}
	
	public int deleteHouse(Connection conn, int houseid) {
		int result = 0;
		
        String sql = "delete from house where houseid = ?";
        
        PreparedStatement ps;
		try {
			ps = conn.prepareStatement(sql);
	        ps.setInt(1, houseid);
	        result = ps.executeUpdate();

	        ps.close();
		} catch (SQLException e) {
			System.out.println("Error in deleteHouse(): " + e.getMessage());
			return 0;
        }
		
		return result;
	}
	
	private int updatePerson(Connection conn, int targetperson, int spouse) {
		int result = 0;
		
        String sql = "update person set spouse=? where personid=?";
        
        PreparedStatement ps;
		try {
			ps = conn.prepareStatement(sql);
	        ps.setInt(1, spouse);
	        ps.setInt(2, targetperson);
	        result = ps.executeUpdate();
	        System.out.println("updatePerson result " + result);

	        ps.close();
		} catch (SQLException e) {
			System.out.println("Error in updatePerson(): " + e.getMessage());
        }
		
		return result;
		
	}
	
	public int deletePerson(Connection conn, int personid) {
		int result = 0;
		
        String sql = "delete from person where personid = ?";
        
        PreparedStatement ps;
		try {
			ps = conn.prepareStatement(sql);
	        ps.setInt(1, personid);
	        result = ps.executeUpdate();

	        ps.close();
		} catch (SQLException e) {
			System.out.println("Error in deletePerson(): " + e.getMessage());
			return 0;
        }
		
		return result;
	}
	
	private boolean isValidSpouse(int personid) {
		boolean ret = false;
		
        String sql = "select * from household hh, person p where hh.personid = p.personid "
        		+ "and hh.personid = ? "
        		+ "and p.maritalid='" + MOMConstants.MARITAL_ID_MARRIED + "' ";
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
			System.out.println("Error in isValidSpouse(): " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }		
		
		return ret;
	}
		
	@Override
	public int insertHouseholdMember(int houseid, String name, String gender, String maritalid,
			String spouse, String occupationid, double annualincome, Date dob) {
		
        Connection conn = getConnection(false);
		
		int personid = insertPerson(conn, name, gender, maritalid, spouse, occupationid, annualincome, dob, houseid);

		String sql = "insert into HOUSEHOLD (houseid, personid) VALUES (?,?)";
        PreparedStatement ps;
        
		try {
			if (personid == -1) {
				System.out.println("Error in insertHouseholdMember(): Failed to insert PERSON.");
				conn.rollback();
			} else if (personid == -2) {
				System.out.println("Error in insertHouseholdMember(): Failed to insert PERSON beause the spouse's personid is invalid.");
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
	public int deleteHouseholdMember(int personid) {
        int result = 0;

		if (!isPersonIdExists(personid)) {
			return 0;
		}

		int houseid = getHouseidOfPerson(personid);
		
        String sql = "delete from household where houseid = ?";
        Connection conn = getConnection(false);
        PreparedStatement ps;
		try {
			ps = conn.prepareStatement(sql);
	        ps.setInt(1, houseid);
	        result = ps.executeUpdate();
	        
	        ps.close();
	        
	        // Delete the related records from HOUSE and PERSON table
	        if (result > 0) {
        		if (deletePerson(conn, personid) <= 0) {
        			result = 0;
        		}
	        }
	        
	        if (result > 0)
	        	conn.commit();
	        else 
	        	conn.rollback();
		} catch (SQLException e) {
			System.out.println("Error in deleteHouseholdMember(): " + e.getMessage());
			try {
				conn.rollback();
			} catch (SQLException e1) {
	            throw new RuntimeException(e1);
			}
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }

		return result;
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
			System.out.println("Error in getValidMaritalIdList(): " + e.getMessage());
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

		List<HouseholdMap> housememberList = getHouseholdMembers(houseid);
		if (housememberList != null) {
			householdList = new ArrayList<Household>();
			
			int uniquehouseid = -1;
			List<Person> personList = new ArrayList<Person>();
			for (int i=0; i<housememberList.size(); i++) {
				HouseholdMap member = housememberList.get(i);
				
				if (i == 0) {
					uniquehouseid = member.getHouseid();
				} else if (uniquehouseid != member.getHouseid()) {
					householdList.add(new Household(uniquehouseid, getHouseholdType(uniquehouseid), personList));
					
					uniquehouseid = member.getHouseid();
					personList = new ArrayList<Person>();
				}
				
				if (member.getPersonid() > 0) {
					personList.add(getPerson(member.getPersonid()));
				}
				if (i == housememberList.size()-1) {
					householdList.add(new Household(uniquehouseid, getHouseholdType(uniquehouseid), personList));
				}
			}
		}
		
		return householdList;
	}
	
//	@Override
//	public List<HouseholdNoSpouse> retrieveHouseholdNoSpouse(String houseid) {
//		List<HouseholdNoSpouse> householdList = new ArrayList<HouseholdNoSpouse>();
//
//		List<HouseholdMember> housememberList = getHouseholdMembers(houseid);
//		if (housememberList != null && !housememberList.isEmpty()) {
//			
//			List<PersonNoSpouse> personList = new ArrayList<PersonNoSpouse>();
//			for (HouseholdMember member : housememberList) {
//				if (member.getPersonid() > 0)
//					personList.add(getPersonNoSpouse(member.getPersonid()));
//			}
//			
//			householdList.add(new HouseholdNoSpouse(getHouseholdType(housememberList.get(0).getHouseid()), personList));
//		}
//		
//		return householdList;
//	}
	
	@Override
	public List<Household> retrieveGrantStudentEncBonus() {
		final int agemax = 16;
		final double totalincomemax = 150000.00;
		final boolean isStudent = true;
		
		List<Integer> eligibleHouseIncome = getHouseidWithTotalIncome("<", totalincomemax);
		List<Household> eligibleHouseAge = getHouseWithPersonAge("<", agemax, isStudent);
		
		List<Household> resultList = new ArrayList<Household>();

		if (eligibleHouseIncome != null && eligibleHouseAge != null) {
			for (int i=0; i<eligibleHouseAge.size(); i++) {
				Household h = eligibleHouseAge.get(i);
				
				if (eligibleHouseIncome.contains(h.getHouseid())) {
					resultList.add(h);
				}
			}
		}
		
		return resultList;
	}
	
	@Override
	public List<Household> retrieveGrantFamilyScheme() {
		final int agemax = 18;
		final boolean isStudent = false;
		
		List<Household> houseWithChild = getHouseWithPersonAge("<", agemax, isStudent);
		List<Household> houseWithCouples = getHouseWithHusbandAndWife();
		
		List<Household> resultList = new ArrayList<Household>();

		if (houseWithChild != null && houseWithCouples != null) {
			for (int i=0; i<houseWithCouples.size(); i++) {
				Household h = houseWithCouples.get(i);
				
				for (int j=0; j<houseWithChild.size(); j++) {
					Household hc = houseWithChild.get(j);
					System.out.println("h.houseid: " + h.getHouseid() + ", hc.houseid: " + hc.getHouseid());
					if (h.getHouseid() == hc.getHouseid()) {
						System.out.println("child-parent connection found, add list");
						List<Person> newlist = new ArrayList<Person>();
						newlist.addAll(h.getMembers());
						newlist.addAll(hc.getMembers());
						resultList.add(new Household(h.getHouseid(), h.getHousetype(), newlist));
						break;
					}
				}
				
			}
		}
		
		return resultList;
	}
	
	@Override
	public List<Household> retrieveGrantElderBonus() {
		final int agemin = 50;
		final boolean isStudent = false;

		List<Integer> houseHDB = getHouseWithHouseType(MOMConstants.HOUSETYPE_HDB);
		List<Household> houseWithElderly = getHouseWithPersonAge(">", agemin, isStudent);
		
		List<Household> resultList = new ArrayList<Household>();

		if (houseHDB != null && houseWithElderly != null) {
			for (int i=0; i<houseWithElderly.size(); i++) {
				Household h = houseWithElderly.get(i);
				
				if (houseHDB.contains(h.getHouseid())) {
					resultList.add(h);
				}
			}
		}
		
		return resultList;
	}
	
	@Override
	public List<Household> retrieveGrantBabySunshine() {
		final int agemax = 5;
		final boolean isStudent = false;

		return getHouseWithPersonAge("<", agemax, isStudent);
	}
	
	@Override
	public List<House> retrieveGrantYOLO() {
		final double totalincomemax = 100000.00;
		
		return getHouseWithTotalIncome("<", totalincomemax);
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

	private List<HouseholdMap> getHouseholdMembers(String houseid) {
		List<HouseholdMap> ret = new ArrayList<HouseholdMap>();
		
		List<House> emptyHouses = getEmptyHouses(houseid);
		if (emptyHouses != null && !emptyHouses.isEmpty()) {
			for (House h : emptyHouses)
				ret.add(new HouseholdMap(h.getHouseid(), 0));
		}
		
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
            
            if (rs.next()) {
            	do {
            		ret.add( new HouseholdMap(rs.getInt("houseid"), rs.getInt("personid")) );
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
	
	private String getHouseholdType(int houseid) {
		String ret = null;
		
		String sql = "select ht.housetype from house h, cfg_house_type ht " +
				"where h.housetypeid=ht.housetypeid and h.houseid = ?";

        Connection conn = getConnection(true);
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, houseid);

            ResultSet rs = ps.executeQuery();
            
            if (!rs.next()) {
            	ret = null;
            } else {
            	ret = rs.getString(1);
            }
            
            rs.close();
            ps.close();
        } catch (SQLException e) {
			System.out.println("Error in getHouseholdType(): " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }		
		
		return ret;
	}
	
	private Person getPerson(int personid) {
		Person ret = null;
		
        String sql = "select p.*, "
        		+ "CASE WHEN gender='M' THEN 'Male' ELSE 'Female' END AS gendertext, "
        		+ "(select maritalstatus from cfg_marital_status where maritalid=p.maritalid) as maritaltext, "
        		+ "(select occupationtype from cfg_occupation where occupationid=p.occupationid) as occupationtext "
        		+ "from person p "
        		+ "where p.personid = ? ";
        Connection conn = getConnection(true);
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, personid);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next())
            	ret = new Person(rs.getInt("personid"), rs.getString("name"), rs.getString("gendertext"), 
            			rs.getString("maritaltext"), rs.getInt("spouse"), rs.getString("occupationtext"),
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

	private int getHouseidOfPerson(int personid) {
		int ret = 0;
		
        String sql = "SELECT HOUSEID FROM HOUSEHOLD WHERE PERSONID=?";
        Connection conn = getConnection(true);
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, personid);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next())
            	ret = rs.getInt(1);
            	
            rs.close();
            ps.close();
        } catch (SQLException e) {
			System.out.println("Error in getHouseOfPerson(): " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }		
		
		return ret;
	}

//	private PersonNoSpouse getPersonNoSpouse(int personid) {
//		PersonNoSpouse ret = null;
//		
//        String sql = "select p.*, "
//        		+ "CASE WHEN gender='M' THEN 'Male' ELSE 'Female' END AS gendertext, "
//        		+ "(select maritalstatus from cfg_marital_status where maritalid=p.maritalid) as maritaltext, "
//        		+ "(select occupationtype from cfg_occupation where occupationid=p.occupationid) as occupationtext "
//        		+ "from person p "
//        		+ "where p.personid = ? ";
//        Connection conn = getConnection(true);
//        
//        try {
//            PreparedStatement ps = conn.prepareStatement(sql);
//            ps.setInt(1, personid);
//            ResultSet rs = ps.executeQuery();
//            
//            if (rs.next())
//            	ret = new PersonNoSpouse(rs.getString("name"), rs.getString("gendertext"), 
//            			rs.getString("maritaltext"), rs.getString("occupationtext"),
//            			rs.getDouble("annualincome"), new Date(rs.getTimestamp("dob").getTime()));
//            	
//            rs.close();
//            ps.close();
//        } catch (SQLException e) {
//			System.out.println("Error in getPersonNoSpouse(): " + e.getMessage());
//            throw new RuntimeException(e);
//        } finally {
//        	closeConnection(conn);
//        }		
//		
//		return ret;
//	}

	private List<Integer> getHouseidWithTotalIncome(String condition, double amount) {
		List<Integer> ret = new ArrayList<Integer>();
		
        String sql = "select hh.houseid, sum(p.annualincome) as totalincome "
        		+ "from household hh, person p "
        		+ "where hh.personid = p.personid "
        		+ "group by hh.houseid ";
        
        if (condition.equals("<") || condition.equals("<=") || condition.equals("=") ||
        		condition.equals(">") || condition.equals(">=") || condition.equals("<>")) {
            sql += "having sum(p.annualincome) " + condition + " ? ";
        } else {
        	return ret;
        }
        Connection conn = getConnection(true);
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDouble(1, amount);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
            	ret = new ArrayList<Integer>();
            	do {
            		ret.add(rs.getInt(1));
            	} while (rs.next());
            }
            	
            rs.close();
            ps.close();
        } catch (SQLException e) {
			System.out.println("Error in getHouseidWithTotalIncome(): " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }		
		
		return ret;
	}

	private List<House> getHouseWithTotalIncome(String condition, double amount) {
		List<House> ret = new ArrayList<House>();
		
        String sql = "select hh.houseid, h.housetypeid, sum(p.annualincome) as totalincome "
        		+ "from household hh, person p, house h "
        		+ "where hh.personid = p.personid and hh.houseid = h.houseid "
        		+ "group by hh.houseid ";
        
        if (condition.equals("<") || condition.equals("<=") || condition.equals("=") ||
        		condition.equals(">") || condition.equals(">=")) {
            sql += "having sum(p.annualincome) " + condition + " ? ";
        } else {
        	return ret;
        }
        Connection conn = getConnection(true);
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDouble(1, amount);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
            	do {
            		ret.add(new House(rs.getInt("houseid"), rs.getString("housetypeid")));
            	} while (rs.next());
            }
            	
            rs.close();
            ps.close();
        } catch (SQLException e) {
			System.out.println("Error in getHouseWithTotalIncome(): " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }		
		
		return ret;
	}

	private List<Integer> getHouseWithHouseType(String housetypeid) {
		List<Integer> ret = new ArrayList<Integer>();
		
        String sql = "select distinct hh.houseid "
        		+ "from household hh, house h "
        		+ "where hh.houseid=h.houseid and h.housetypeid='" + housetypeid + "' ";
        
        Connection conn = getConnection(true);
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
            	ret = new ArrayList<Integer>();
            	do {
            		ret.add(rs.getInt(1));
            	} while (rs.next());
            }
            	
            rs.close();
            ps.close();
        } catch (SQLException e) {
			System.out.println("Error in getHouseWithHouseType(): " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }		
		
		return ret;
	}

	private List<Household> getHouseWithHusbandAndWife() {
		List<Household> ret = new ArrayList<Household>();
		
        String sql = "select hh.houseid, h.housetypeid, "
        		+ "(select housetype from cfg_house_type where housetypeid = h.housetypeid) as housetype, p.*, "
        		+ "CASE WHEN gender='M' THEN 'Male' ELSE 'Female' END AS gendertext, "
        		+ "(select maritalstatus from cfg_marital_status where maritalid=p.maritalid) as maritaltext, "
        		+ "(select occupationtype from cfg_occupation where occupationid=p.occupationid) as occupationtext "
        		+ "from person p, house h, household hh "
        		+ "where hh.personid = p.personid "
        		+ "and hh.houseid=h.houseid "
        		+ "and exists "
        		+ "(select pp.personid "
        		+ " from person pp, household hhhh "
        		+ " where hhhh.personid = p.personid "
        		+ " and pp.maritalid = '" + MOMConstants.MARITAL_ID_MARRIED + "' and pp.spouse = p.personid and hhhh.houseid = hh.houseid) "
        		+ "order by hh.houseid asc; ";
        
        Connection conn = getConnection(true);
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
            	int uniquehouseid = rs.getInt("houseid");
            	String uniquehousetype = rs.getString("housetype");
            	List<Person> personList = new ArrayList<Person>();
            	do {
            		if (uniquehouseid != rs.getInt("houseid")) {
            			ret.add(new Household(uniquehouseid, uniquehousetype, personList));

            			uniquehouseid = rs.getInt("houseid");
                		uniquehousetype = rs.getString("housetype");
                		personList = new ArrayList<Person>();
            		}
            		personList.add(new Person(rs.getInt("personid"), rs.getString("name"), rs.getString("gendertext"), 
                			rs.getString("maritaltext"), rs.getInt("spouse"), rs.getString("occupationtext"),
                			rs.getDouble("annualincome"), new Date(rs.getTimestamp("dob").getTime())));
            	} while (rs.next());
            	ret.add(new Household(uniquehouseid, uniquehousetype, personList));
            }
            	
            rs.close();
            ps.close();
        } catch (SQLException e) {
			System.out.println("Error in getHouseWithHusbandAndWife(): " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }		
		
		return ret;
	}

	private List<Household> getHouseWithPersonAge(String condition, int age, boolean isStudent) {
		List<Household> ret = new ArrayList<Household>();
		
        String sql = "select hh.houseid, h.housetypeid, "
        		+ "(select housetype from cfg_house_type where housetypeid = h.housetypeid) as housetype, "
        		+ "TIMESTAMPDIFF(YEAR, p.dob, CURDATE()) AS age, p.*, "
        		+ "CASE WHEN gender='M' THEN 'Male' ELSE 'Female' END AS gendertext, "
        		+ "(select maritalstatus from cfg_marital_status where maritalid=p.maritalid) as maritaltext, "
        		+ "(select occupationtype from cfg_occupation where occupationid=p.occupationid) as occupationtext "
        		+ "from household hh, person p, house h "
        		+ "where hh.personid = p.personid "
        		+ "and hh.houseid = h.houseid ";
        
        if (condition.equals("<") || condition.equals("<=") || condition.equals("=") ||
        		condition.equals(">") || condition.equals(">=") || condition.equals("<>")) {
        	sql += "and TIMESTAMPDIFF(YEAR, p.dob, CURDATE()) " + condition + " ? ";
        } else {
        	return null;
        }
        
        if (isStudent) {
    		sql += "and p.occupationid = '" + MOMConstants.OCCUPATION_STUDENT + "' ";
        }
		sql += "order by hh.houseid asc ";
        
        Connection conn = getConnection(true);
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, age);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
            	int uniquehouseid = rs.getInt("houseid");
            	String uniquehousetype = rs.getString("housetype");
            	List<Person> personList = new ArrayList<Person>();
            	do {
            		if (uniquehouseid != rs.getInt("houseid")) {
            			ret.add(new Household(uniquehouseid, uniquehousetype, personList));

            			uniquehouseid = rs.getInt("houseid");
                		uniquehousetype = rs.getString("housetype");
                		personList = new ArrayList<Person>();
            		}
            		personList.add(new Person(rs.getInt("personid"), rs.getString("name"), rs.getString("gendertext"), 
                			rs.getString("maritaltext"), rs.getInt("spouse"), rs.getString("occupationtext"),
                			rs.getDouble("annualincome"), new Date(rs.getTimestamp("dob").getTime())));
            	} while (rs.next());
            	ret.add(new Household(uniquehouseid, uniquehousetype, personList));
            }
            	
            rs.close();
            ps.close();
        } catch (SQLException e) {
			System.out.println("Error in getHouseWithPersonAge(): " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }		
		
        return ret;
	}

	private List<House> getEmptyHouses(String houseid) {
		List<House> houseList = new ArrayList<House>();
		
        String sql = "SELECT * FROM HOUSE H WHERE NOT EXISTS (SELECT HOUSEID FROM HOUSEHOLD WHERE HOUSEID=H.HOUSEID) ";
        if (!houseid.equalsIgnoreCase("all")) {
            sql += "AND HOUSEID = ? ";
        }
        sql += "ORDER BY HOUSEID ASC";
        Connection conn = getConnection(true);
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            if (!houseid.equalsIgnoreCase("all")) {
                ps.setString(1, houseid);
            }
            ResultSet rs = ps.executeQuery();
            
            while (rs.next())
            	houseList.add(new House(rs.getInt("houseid"), rs.getString("housetypeid")));
            
            rs.close();
            ps.close();
        } catch (SQLException e) {
			System.out.println("Error in getEmptyHouses(): " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
        	closeConnection(conn);
        }
        
        return houseList;
	}

	
}
