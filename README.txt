NOTE: This guide assume you are using Windows environment.

MYSQL INSTALLATION
------------------
1. Download and install MySQL Community:
https://dev.mysql.com/downloads/installer/

2. Execute the installation: Choose Developer Default installation
   * At the Check Requirements, resolve any missing requirements before proceeding.
   * At Installation, click on Execute to install MySQL. You'll be then able to configure at the next step.
   a) Type and Networking
      Config Type: Development Computer
	  TCP/IP checked: Default port 3306 and X protocol port 33060
	  Default is checked for Open Window Firewall ports
	  Click Next when done
   b) Authentication Method
      Just leave it with default values (Use Strong Password Encryption)
	  Click Next button
   c) Accounts and Roles
      Root password: root
      Click on Add User:
        User Name: momtest
        Host: <All Hosts (%)>
        Role: DB Admin
        Authentication: Select "MySQL"
        Password: momtest
		Click OK button when done
      Click Next when done
   d) Windows Service
      Checked "Configure MySQL Server as a Windows Service"
      Just use default values
	  Run Windows Service as: Standard System Account
   e) Apply Configuration
      Click on Execute button, let it run until finished
   f) Product Configuration - Samples and Examples
      Select the available server, User/Password is root/root
   g) Continue until installation is finished

3. SQL script files can be found under the "sql" folder from the downloaded source codes:
- 0_create_database.sql
- 1_create_tables.sql
- 2_insert_config.sql
You may want to copy it to a convenient path so it is not too long (eg. C:\tmp folder)

4. Open MySQL 8.0 Command Line Client shortcut, enter root password (root)
Execute scripts with the commands below (for example if the sql script files under C:\tmp folder):
source C:\tmp\0_create_database.sql
source C:\tmp\1_create_tables.sql
source C:\tmp\2_insert_config.sql

When done, all the tables should be created and ready to use.
To test, try to run a query below:
select * from house;

It should return en empty row.

RUNNING SOURCE LOCALLY
----------------------
1. Download source codes from GitHub:
   https://github.com/m3-julius/demomom

2. Using Eclipse IDE, choose to Import Existing Maven Project and select the folder where you downloaded the source codes.
   File -> Import -> Existing Maven Project (elect the pom.xml item and click Finish)

3. Let Eclipse build the project or from the menu: Project -> Clean
   Note: Please ensure the Eclipse is configured for a JDK environment. This application requires JDK libraries.
         If necessary, please set a proper JRE environment from Project Properties -> Java Build Path -> Libraries tab -> JRE System Library -> Edit and point to jdk. You may download and install the jdk from Oracle website and add it into your eclipse configuration.

4. In case required to compile and build the package manually, right click on demo project and select Run As -> Maven install
   "BUILD SUCCESS" from console should be displayed when done. First run is longer as Eclipse will download the libraries.

5. To run the application, execute from command prompt at the source codes folder (Windows):
   .\mvnw spring-boot:run
   You should see a log "...Application availability state ReadinessState changed to ACCEPTING_TRAFFIC"
   This means the application is ready and listening to the local port (8080).
   Note: First run is longer as the application will download the libraries.

6. To terminate, press Ctrl+C from the command prompt and answer y to terminate.

REST Endpoints Usage
--------------------
1. You may use your favorite browser to execute the link.
   Recommended way is to use curl from command line.
   The guides below is using curl command for execution.
   This application is listening at localhost:8080
   You may also access this link to get the generated API documentation:
   http://localhost:8080/api-docs/

2. Below are the valid endpoints to use:
NOTE: Anything that is wrapped with sharp brackets <value> is to be replaced with a proper input value.

a) Create house
curl -i "http://localhost:8080/createhouse?housetype=<housetype>"
<housetype>: Required - H [HDB], C [Condo] or L [Landed]

Example:
curl -i "http://localhost:8080/createhouse?housetype=H"

b) Create a household member and link it to a house
curl -i "http://localhost:8080/createhouseholdmember?houseid=<houseid>&name=<name>&gender=<gender>&maritalid=<maritalid>&spouse=<spouse>&occupationid=<occupationid>&annualincome=<annualincome>&dob=<dob>"
<houseid> input: Required - Valid houseid numeric (must be existing in DB)
<name> input: Required - Alphanumeric and whitespaces (Whitespace ' ' in the URL link must be replaced to '%20')
<gender> input: Required - M [Male] or F [Female]
<maritalid> input: Required - S [Single] or M [Married]
<spouse> input: Optional - Valid personid numeric (must be existing in DB)
<occupationid> input: Required - ST [Student], UN [Unemployed] or EM [Employed]
<annualincome> input: Optional - Default value 0.00 - Decimal numbers
<dob> input: Required - Date pattern is ddmmyyyy (example: 05092020 [for 5 Sep 2020])

Assumptions:
- A person must be attached to the household mapping table. This is following the assessment's request.
  With that assumption, I don't create separate endpoints to insert a standalone person & a mapping to a house.
- To make a couple link, first create a new member without entering the spouse (personid).
  After that, create a new second member with spouse value.
  The application will automatically detect if the spouse personid exists in the DB, in Married status and the spouse value is still null.
  If yes, the first person's spouse value will be updated to the second person's personid.
  If no, the second member insertion is deemed as failed and insertion will be rollbacked.
  Illustration as below:
  1st member insertion:
	name	personid	maritalid	spouse
	Jack	2			M			null
  2nd member insertion:
	name	personid	maritalid	spouse
	Jill	3			M			2
  Jack's record will then updated during Jill record's insertion if inputs are all valid. Below is the final result:
	name	personid	maritalid	spouse
	Jack	2			M			3
	Jill	3			M			2
- A married couple in this application only accepts monogamy couple.
  From the illustation above, if there is another insertion with spouse value of a personid that already has a mapping, the insertion
  will be rejected.

Example:
(without spouse and annualincome parameters)
curl -i "http://localhost:8080/createhouseholdmember?houseid=1&name=John%20Doe&gender=M&maritalid=M&occupationid=UN&dob=11102018"

(complete parameters)
curl -i "http://localhost:8080/createhouseholdmember?houseid=1&name=Jane%20Jackson&gender=F&maritalid=M&spouse=2&occupationid=EM&annualincome=50000.25&dob=11102018"

c) List all households with its family members
curl -i "http://localhost:8080/gethousehold?houseid=all"

d) Show a household details with its family members
curl -i "http://localhost:8080/gethousehold?houseid=<houseid>"
<houseid> input: Required - Valid houseid integer (must be existing in DB)

Example:
curl -i "http://localhost:8080/gethousehold?houseid=3"

e) List for eligible Student Encouragement Bonus grant
curl -i "http://localhost:8080/liststudentgrant"

Assumptions:
- My interpretation of the requirement is to return only the students, so not including parents in this list.

f) List for eligible Family Togetherness Scheme grant
curl -i "http://localhost:8080/listfamilygrant"

Assumptions:
- Both couple must live at the same house and has at least 1 child.
- Will list both parents and the eligible child(ren) for each household.

g) List for eligible Elder Bonus grant
curl -i "http://localhost:8080/listeldergrant"

Assumptions:
- My interpretation of the requirement is to return only the elderly members, so not including parents in this list.

h) List for eligible Baby Sunshine grant
curl -i "http://localhost:8080/listbabygrant"

Assumptions:
- My interpretation of the requirement is to return only the child(ren) members, so not including parents in this list.

i) List for YOLO GST grant
curl -i "http://localhost:8080/listyologrant"

j) Delete a household and its family members
curl -i "http://localhost:8080/deletehousehold?houseid=<houseid>"
<houseid> input: Required - Valid houseid numeric (must be existing in DB)

Example:
curl -i "http://localhost:8080/deletehousehold?houseid=12"

Assumptions:
- House data will be deleted (Interpreted this as if the house is being demolished and doesn't exist anymore)
- All family members will be deleted.

k) Delete a family member from a household
curl -i "http://localhost:8080/deletehouseholdmember?personid=<personid>"
<personid> input: Required - Valid personid numeric (must be existing in DB)

Example:
curl -i "http://localhost:8080/deletehouseholdmember?personid=18"





		







