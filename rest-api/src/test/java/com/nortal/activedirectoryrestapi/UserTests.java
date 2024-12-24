package com.nortal.activedirectoryrestapi;

import com.nortal.activedirectoryrestapi.controllers.RESTApiController;
import com.nortal.activedirectoryrestapi.entities.Command;
import com.nortal.activedirectoryrestapi.services.CommandService;
import com.nortal.activedirectoryrestapi.services.CommandWorker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserTests {

    @LocalServerPort
    private int port;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }


    @Mock
    private CommandWorker commandWorker;

    @Mock
    private CommandService commandService;

    private TestRestTemplate restTemplate;

    private String createdUserSamAccountName;

    private Helper helper = new Helper();


    @BeforeEach
    public void setUp() {
        restTemplate = new TestRestTemplate();
    }

    @AfterEach
    public void tearDown() {
        if (createdUserSamAccountName != null) {
            helper.deleteIfExists(createdUserSamAccountName, getBaseUrl()+"/users");
        }
    }


    @Test
    public void testCreateNewUser() throws Exception {
        String payload = "{"
                + "\"Name\": \"Test User\","
                + "\"GivenName\": \"Test\","
                + "\"Surname\": \"User\","
                + "\"SamAccountName\": \"testuser\","
                + "\"UserPrincipalName\": \"testuser@domain.com\","
                + "\"Path\": \"CN=Users,DC=Domain,DC=ee\","
                + "\"Enabled\": true,"
                + "\"AccountPassword\": \"ComplexP@ssw0rd4567\""
                + "}";

        createdUserSamAccountName = "testuser";

        Command mockCommand = new Command();
        mockCommand.setCommand("New-ADUser");
        mockCommand.setArguments(payload);
        mockCommand.setExitCode(0);
        mockCommand.setId(1L);

        //when(commandWorker.executeCommand("New-ADUser", payload)).thenReturn(mockCommand);
        //when(commandService.getCommand(mockCommand.getId())).thenReturn(mockCommand);

        String url = getBaseUrl() + "/users";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        //Command savedCommand = commandService.getCommand(mockCommand.getId());
        //assertNotNull(savedCommand);
        //assertEquals("New-ADUser", savedCommand.getCommand());
        //assertEquals(payload, savedCommand.getArguments());
        //assertEquals(0, savedCommand.getExitCode());
    }

    @Test
    public void testUpdateUser() throws Exception {

        helper.createTestUser(getBaseUrl()+"/users", commandWorker);

        String updatePayload = "{"
                + "\"Identity\": \"testuser\","
                + "\"GivenName\": \"Test\","
                + "\"Surname\": \"User\","
                + "\"SamAccountName\": \"testuserupdate\","
                + "\"UserPrincipalName\": \"testuser@domain.com\","
                + "\"Enabled\": true"
                + "}";

        Command mockUpdateCommand = new Command();
        mockUpdateCommand.setCommand("Set-ADUser");
        mockUpdateCommand.setArguments(updatePayload);
        mockUpdateCommand.setExitCode(0);
        mockUpdateCommand.setId(3L);

        //when(commandWorker.executeCommand("Set-ADUser", updatePayload)).thenReturn(mockUpdateCommand);
        //when(commandService.getCommand(mockUpdateCommand.getId())).thenReturn(mockUpdateCommand);

        String updateUrl = getBaseUrl() + "/users";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> updateEntity = new HttpEntity<>(updatePayload, headers);
        ResponseEntity<String> updateResponse = restTemplate.exchange(updateUrl, HttpMethod.PUT, updateEntity, String.class);

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        createdUserSamAccountName = "testuserupdate";
    }

    @Test
    public void testGetUsers() throws Exception {
        MultiValueMap<String, Object> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("Filter", "*");
        queryParams.add("SearchBase", "DC=Domain,DC=ee");

        String mockCommand = "Get-ADUser";
        Command command = new Command();
        command.setCommand(mockCommand);
        command.setArguments(queryParams.toString());
        command.setExitCode(0);

        String mockJson = "{\"Filter\":\"*\",\"SearchBase\":\"DC=Domain,DC=ee\"}";
        //when(commandWorker.executeCommand(mockCommand, mockJson)).thenReturn(command);

        String url = getBaseUrl() + "/users?Filter=*&SearchBase=DC=Domain,DC=ee";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(null), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("testuser2"));
    }

}
