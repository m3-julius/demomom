connect momdb;

CREATE TABLE CFG_HOUSE_TYPE (
    housetypeid varchar(1) NOT NULL,
    housetype varchar(255) NOT NULL,
    PRIMARY KEY (housetypeid)
) ENGINE=INNODB;

CREATE TABLE CFG_MARITAL_STATUS (
    maritalid varchar(1) NOT NULL,
    maritalstatus varchar(255) NOT NULL,
    PRIMARY KEY (maritalid)
) ENGINE=INNODB;

CREATE TABLE CFG_OCCUPATION (
    occupationid varchar(2) NOT NULL,
    occupationtype varchar(255) NOT NULL,
    PRIMARY KEY (occupationid)
) ENGINE=INNODB;

CREATE TABLE HOUSE (
    houseid int NOT NULL AUTO_INCREMENT,
    housetypeid varchar(1) NOT NULL,
    PRIMARY KEY (houseid),
	FOREIGN KEY (housetypeid) REFERENCES CFG_HOUSE_TYPE(housetypeid)
) ENGINE=INNODB;

CREATE TABLE PERSON (
    personid int NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    gender varchar(1) NOT NULL,
    maritalid varchar(1) NOT NULL,
	spouse int,
	occupationid varchar(2) NOT NULL,
	annualincome decimal(9,2),
    dob datetime NOT NULL,
    PRIMARY KEY (personid)
) ENGINE=INNODB;

CREATE TABLE HOUSEHOLD (
    houseid int NOT NULL AUTO_INCREMENT,
    personid int NOT NULL,
    PRIMARY KEY (houseid, personid),
	FOREIGN KEY (houseid) REFERENCES HOUSE(houseid),
	FOREIGN KEY (personid) REFERENCES PERSON(personid)
) ENGINE=INNODB;
