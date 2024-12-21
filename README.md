# Active Directory Manager

A software component designed to send queries to the Active Directory and retrieve the responses.

---

## How to Use the Application

### 1. PostgreSQL Database Setup
Ensure that a PostgreSQL database is running.

### 2. Set Environment Variables
Before starting the application, set the following environment variables. Make sure to update your database credentials.

```bash
db_user=adm
db_password=password
db_host=localhost
db_name=active_directory_commands
db_port=5432
```
### 3. Start the Spring Boot Application
Go to the `rest-api` folder and run the app:
```bash
./gradlew bootRun
```

### 4. Build Docker Image
Go to the `cmdlets` folder and build the Docker image using the following command (`docker` is substitutable with `podman` in this and the following steps):

```bash
docker build -t adm .
```

### 5. Run the Docker Container
Make sure that you are executing this step only when necessary table is already created by Spring App. Otherwise the program will crash. You need to have a working Windows Server with Active Directory enabled for this step.
Run the container, which will execute the added command with the following command:

```bash
docker run -it --rm \
-e ADServer="server-ip" \
-e ADUsername="user" \
-e ADPassword="password" \
-e db_user="adm" \
-e db_password="password" \
-e db_host="localhost" \
-e db_name="active_directory_commands" \
-e db_port="5432" \
--network host adm
```
**NB!** Do not forget to change your database and Windows Server (AD) credentials. Also if you are using Docker Desktop do following steps: 
1. Sign in to your Docker account in Docker Desktop.
2. Navigate to Settings.
3. Under the Resource tab, select Network.
4. Check the Enable host networking option.
5. Select Apply and restart.

### 6. Access Swagger UI
You can access the Swagger UI at:

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### 7. Make a HTTP request
The payload (or query parameters in case of GET/DELETE requests) should contain arguments that you want to pass to a relevant AD command. Switch parameters should have a 'true' value. Refer to [Active Directory Module documentation] (https://learn.microsoft.com/en-us/powershell/module/activedirectory/) for the reference. The only known exceptions to the documentation are passwords (AccountPassword, OldPassword, NewPassword), which have to be specified as plaintext (as shown here) and later converted to PowerShell SecureString by the app. **Please note that passwords supplied this way are not secure and should be changed on first log on.**

## Example payloads

- **Example for Adding a New User:**
/users - POST
```json
{
  "Name": "Ryan Gosling",
  "GivenName": "Ryan",
  "Surname": "Gosling",
  "SamAccountName": "ryangosling",
  "Path": "CN=Users,DC=Domain,DC=ee", 
  "Enabled": true,
  "AccountPassword": "ComplexP@ssw0rd4567"
}
```

- **Example for getting all users:**
/users - GET
```json
{
  "Filter": "*",
  "SearchBase": "DC=Domain,DC=ee"
}
```

```json
{
    "Identity": "CN=Ryan Gosling,CN=Users,DC=Domain,DC=ee"
}
```

/groups - POST
```json
{
 "Name": "Drive Cast",
 "SamAccountName": "DriveCast",
 "GroupCategory": "Security",
 "GroupScope": "Global",
 "DisplayName": "Drive Cast",
 "Path": "CN=Users,DC=Domain,DC=ee",
 "Description": "Members of this group are part of Drive movie cast"
}
```
/groups/members - POST
```json
{
    "Identity": "CN=Drive Cast,CN=Users,DC=Domain,DC=ee",
    "Members": ["CN=Ryan Gosling,CN=Users,DC=Domain,DC=ee"]
}
```

/groups - GET
```json
{
    "Identity": "CN=Drive Cast,CN=Users,DC=Domain,DC=ee"
}
```

### 8. View Command Output
After command execution, you will see the command output (in JSON format) together with a relevant status code in the Swagger UI response. 

### Jenkins Pipeline for Automated Testing and Building

There is also a Jenkins pipeline that automates the testing and building process for this application. The pipeline executes tests, builds the Spring Boot application, and creates a Docker container for deployment.

You can view and trigger the Jenkins job here:

[Jenkins Pipeline - Active Directory Manager](https://srv620081.hstgr.cloud/job/ActiveDirectory/)

The pipeline performs the following steps:
1. **Test Execution:** Runs automated tests to ensure that the application is functioning as expected.
2. **Build Spring Boot App:** Compiles and builds the Spring Boot application.
3. **Build Docker Image:** Creates a Docker image of the application, which can then be deployed to a server.

This pipeline ensures that the application is tested and built in a repeatable and automated manner, reducing the potential for errors during manual setup.



