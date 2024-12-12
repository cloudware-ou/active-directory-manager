package com.nortal.activedirectoryrestapi;

import com.nortal.activedirectoryrestapi.controllers.RESTApiController;
import com.nortal.activedirectoryrestapi.entities.Commands;
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
public class GroupsTests {

    @LocalServerPort
    private int port;

    @InjectMocks
    private RESTApiController restApiController;

    @Mock
    private CommandWorker commandWorker;

    @Mock
    private CommandService commandService;

    private TestRestTemplate restTemplate;

    private String createdGroupName;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    public void setUp() {
        restTemplate = new TestRestTemplate();
    }

    @AfterEach
    public void tearDown() {
        if (createdGroupName != null) {
            deleteGroupIfExists(createdGroupName);
            createdGroupName = null; // Reset for next test
        }
    }

    private void deleteGroupIfExists(String groupName) {
        String deleteUrl = getBaseUrl() + "/groups";
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("Identity", groupName);

        HttpEntity<MultiValueMap<String, Object>> deleteEntity = new HttpEntity<>(params);

        restTemplate.exchange(
                deleteUrl,
                HttpMethod.DELETE,
                deleteEntity,
                String.class
        );
    }

    @Test
    public void testCreateNewGroup() throws Exception {
        String payload = "{" +
                "\"Name\": \"TestGroup2\"," +
                "\"GroupScope\": \"Global\"," +
                "\"GroupCategory\": \"Security\"" +
                "}";

        createdGroupName = "TestGroup2";

        Commands mockCommand = new Commands();
        mockCommand.setCommand("New-ADGroup");
        mockCommand.setArguments(payload);
        mockCommand.setExitCode(0);
        mockCommand.setId(4L);

        when(commandWorker.executeCommand("New-ADGroup", payload)).thenReturn(mockCommand);
        when(commandService.getCommand(mockCommand.getId())).thenReturn(mockCommand);

        String url = getBaseUrl() + "/groups";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Commands savedCommand = commandService.getCommand(mockCommand.getId());
        assertNotNull(savedCommand);
        assertEquals("New-ADGroup", savedCommand.getCommand());
        assertEquals(payload, savedCommand.getArguments());
        assertEquals(0, savedCommand.getExitCode());
    }

    @Test
    public void testUpdateGroup() throws Exception {
        String payload = "{" +
                "\"Name\": \"TestGroup3\"," +
                "\"GroupScope\": \"Global\"," +
                "\"GroupCategory\": \"Security\"" +
                "}";

        createdGroupName = "TestGroup3";

        Commands mockCreateCommand = new Commands();
        mockCreateCommand.setCommand("New-ADGroup");
        mockCreateCommand.setArguments(payload);
        mockCreateCommand.setExitCode(0);
        mockCreateCommand.setId(5L);

        when(commandWorker.executeCommand("New-ADGroup", payload)).thenReturn(mockCreateCommand);
        when(commandService.getCommand(mockCreateCommand.getId())).thenReturn(mockCreateCommand);

        String createUrl = getBaseUrl() + "/groups";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> createResponse = restTemplate.exchange(createUrl, HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

        String updatePayload = "{" +
                "\"Identity\": \"TestGroup3\"," +
                "\"SamAccountName\": \"UpdatedTestGroup3\"," +
                "\"GroupScope\": \"Global\"" +
                "}";

        Commands mockUpdateCommand = new Commands();
        mockUpdateCommand.setCommand("Set-ADGroup");
        mockUpdateCommand.setArguments(updatePayload);
        mockUpdateCommand.setExitCode(0);
        mockUpdateCommand.setId(6L);

        when(commandWorker.executeCommand("Set-ADGroup", updatePayload)).thenReturn(mockUpdateCommand);
        when(commandService.getCommand(mockUpdateCommand.getId())).thenReturn(mockUpdateCommand);

        String updateUrl = getBaseUrl() + "/groups";
        HttpEntity<String> updateEntity = new HttpEntity<>(updatePayload, headers);
        ResponseEntity<String> updateResponse = restTemplate.exchange(updateUrl, HttpMethod.PUT, updateEntity, String.class);

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

    }

    @Test
    public void testGetGroups() throws Exception {
        MultiValueMap<String, Object> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("Filter", "*");
        queryParams.add("SearchBase", "DC=Domain,DC=ee");

        String mockCommand = "Get-ADGroup";
        Commands command = new Commands();
        command.setCommand(mockCommand);
        command.setArguments(queryParams.toString());
        command.setExitCode(0);

        String mockJson = "{\"Filter\":\"*\",\"SearchBase\":\"DC=Domain,DC=ee\"}";
        when(commandWorker.executeCommand(mockCommand, mockJson)).thenReturn(command);

        String url = getBaseUrl() + "/groups?Filter=*&SearchBase=DC=Domain,DC=ee";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(null), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("TestGroup1"));
    }
}
