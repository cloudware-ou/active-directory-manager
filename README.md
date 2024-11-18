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
Run the Spring Boot application.

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

- **Example for Adding a New User:**
/users - POST
```json
{
  "Name": "Aliwfededece Johnson",
  "GivenName": "Alifewce",
  "Surname": "Johnfewfsfeson",
  "SamAccountName": "ajofededewhnson012",
  "UserPrincipalName": "ajohnfewdedenson02@domain.com",
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


### 8. View Command Output
After command execution, you will see the command output (in JSON format) together with a relevant status code in the Swagger UI response. 
