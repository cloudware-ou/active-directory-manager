package com.nortal.activedirectoryrestapi;

import com.nortal.activedirectoryrestapi.controllers.RESTApiController;
import com.nortal.activedirectoryrestapi.entities.Commands;
import com.nortal.activedirectoryrestapi.exceptions.ADCommandExecutionException;
import com.nortal.activedirectoryrestapi.services.CommandService;
import com.nortal.activedirectoryrestapi.services.CommandWorker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CommandIntegrationTest {

    @LocalServerPort
    private int port;

    @InjectMocks
    private RESTApiController restApiController;

    @Mock
    private CommandWorker commandWorker;

    @Mock
    private CommandService commandService;

    private TestRestTemplate restTemplate;

    private String createdUserSamAccountName;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    public void setUp() {
        restTemplate = new TestRestTemplate();
    }

    @AfterEach
    public void tearDown() {
        if (createdUserSamAccountName != null) {
            deleteUserIfExists(createdUserSamAccountName);
            createdUserSamAccountName = null; // Reset for next test
        }
    }

    private void deleteUserIfExists(String samAccountName) {
        String deleteUrl = getBaseUrl() + "/users";
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("Identity", samAccountName);

        HttpEntity<MultiValueMap<String, Object>> deleteEntity = new HttpEntity<>(params);

        restTemplate.exchange(
                deleteUrl,
                HttpMethod.DELETE,
                deleteEntity,
                String.class
        );
    }

    @Test
    public void testCreateNewUser() throws Exception {
        String payload = "{"
                + "\"Name\": \"Test3 User\","
                + "\"GivenName\": \"Test3\","
                + "\"Surname\": \"User\","
                + "\"SamAccountName\": \"testuser3\","
                + "\"UserPrincipalName\": \"testuser3@domain.com\","
                + "\"Path\": \"CN=Users,DC=Domain,DC=ee\","
                + "\"Enabled\": true,"
                + "\"AccountPassword\": \"ComplexP@ssw0rd4567\""
                + "}";

        createdUserSamAccountName = "testuser3";

        Commands mockCommand = new Commands();
        mockCommand.setCommand("New-ADUser");
        mockCommand.setArguments(payload);
        mockCommand.setExitCode(0);
        mockCommand.setId(1L);

        when(commandWorker.executeCommand("New-ADUser", payload)).thenReturn(mockCommand);
        when(commandService.getCommand(mockCommand.getId())).thenReturn(mockCommand);

        String url = getBaseUrl() + "/users";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Commands savedCommand = commandService.getCommand(mockCommand.getId());
        assertNotNull(savedCommand);
        assertEquals("New-ADUser", savedCommand.getCommand());
        assertEquals(payload, savedCommand.getArguments());
        assertEquals(0, savedCommand.getExitCode());
    }

    @Test
    public void testUpdateUser() throws Exception {
        String payload = "{"
                + "\"Name\": \"Test4 User\","
                + "\"GivenName\": \"Test4\","
                + "\"Surname\": \"User\","
                + "\"SamAccountName\": \"testuser4\","
                + "\"UserPrincipalName\": \"testuser4@domain.com\","
                + "\"Path\": \"CN=Users,DC=Domain,DC=ee\","
                + "\"Enabled\": true,"
                + "\"AccountPassword\": \"ComplexP@ssw0rd4567\""
                + "}";

        createdUserSamAccountName = "testuser4";

        Commands mockCreateCommand = new Commands();
        mockCreateCommand.setCommand("New-ADUser");
        mockCreateCommand.setArguments(payload);
        mockCreateCommand.setExitCode(0);
        mockCreateCommand.setId(2L);

        when(commandWorker.executeCommand("New-ADUser", payload)).thenReturn(mockCreateCommand);
        when(commandService.getCommand(mockCreateCommand.getId())).thenReturn(mockCreateCommand);

        String createUrl = getBaseUrl() + "/users";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> createResponse = restTemplate.exchange(createUrl, HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

        String updatePayload = "{"
                + "\"Identity\": \"testuser4\","
                + "\"GivenName\": \"Test4\","
                + "\"Surname\": \"User\","
                + "\"SamAccountName\": \"testuser4update\","
                + "\"UserPrincipalName\": \"testuser4@domain.com\","
                + "\"Enabled\": true"
                + "}";

        Commands mockUpdateCommand = new Commands();
        mockUpdateCommand.setCommand("Set-ADUser");
        mockUpdateCommand.setArguments(updatePayload);
        mockUpdateCommand.setExitCode(0);
        mockUpdateCommand.setId(3L);

        when(commandWorker.executeCommand("Set-ADUser", updatePayload)).thenReturn(mockUpdateCommand);
        when(commandService.getCommand(mockUpdateCommand.getId())).thenReturn(mockUpdateCommand);

        String updateUrl = getBaseUrl() + "/users";
        HttpEntity<String> updateEntity = new HttpEntity<>(updatePayload, headers);
        ResponseEntity<String> updateResponse = restTemplate.exchange(updateUrl, HttpMethod.PUT, updateEntity, String.class);

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        createdUserSamAccountName = "testuser4update";
    }

    @Test
    public void testGetUsers() throws Exception {
        MultiValueMap<String, Object> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("Filter", "*");
        queryParams.add("SearchBase", "DC=Domain,DC=ee");

        String mockCommand = "Get-ADUser";
        Commands command = new Commands();
        command.setCommand(mockCommand);
        command.setArguments(queryParams.toString());
        command.setExitCode(0);

        String mockJson = "{\"Filter\":\"*\",\"SearchBase\":\"DC=Domain,DC=ee\"}";
        when(commandWorker.executeCommand(mockCommand, mockJson)).thenReturn(command);

        String url = getBaseUrl() + "/users?Filter=*&SearchBase=DC=Domain,DC=ee";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(null), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("testuser2"));
    }
}
