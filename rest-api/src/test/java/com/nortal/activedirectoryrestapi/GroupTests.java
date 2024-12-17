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
public class GroupTests {

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
    private Helper helper = new Helper();

    @BeforeEach
    public void setUp() {
        restTemplate = new TestRestTemplate();
    }

    @AfterEach
    public void tearDown() {
        if (createdGroupName != null) {
            helper.deleteIfExists("CN="+createdGroupName+",CN=Users,DC=Domain,DC=ee", getBaseUrl()+"/groups");
            createdGroupName = null;
        }
    }


    @Test
    public void testCreateNewGroup() throws Exception {
        String payload = "{" +
                "\"Name\": \"TestGroup1\"," +
                "\"GroupScope\": \"Global\"," +
                "\"GroupCategory\": \"Security\"" +
                "}";

        createdGroupName = "TestGroup1";

        Command mockCommand = new Command();
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

        Command savedCommand = commandService.getCommand(mockCommand.getId());
        assertNotNull(savedCommand);
        assertEquals("New-ADGroup", savedCommand.getCommand());
        assertEquals(payload, savedCommand.getArguments());
        assertEquals(0, savedCommand.getExitCode());
    }

    @Test
    public void testUpdateGroup() throws Exception {
        helper.createGroup(getBaseUrl()+"/groups", commandWorker);

        String updatePayload = "{" +
                "\"Identity\": \"TestGroup2\"," +
                "\"SamAccountName\": \"UpdatedTestGroup\"," +
                "\"GroupScope\": \"Global\"" +
                "}";

        createdGroupName = "TestGroup2";

        Command mockUpdateCommand = new Command();
        mockUpdateCommand.setCommand("Set-ADGroup");
        mockUpdateCommand.setArguments(updatePayload);
        mockUpdateCommand.setExitCode(0);
        mockUpdateCommand.setId(6L);

        when(commandWorker.executeCommand("Set-ADGroup", updatePayload)).thenReturn(mockUpdateCommand);
        when(commandService.getCommand(mockUpdateCommand.getId())).thenReturn(mockUpdateCommand);

        String updateUrl = getBaseUrl() + "/groups";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
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
        Command command = new Command();
        command.setCommand(mockCommand);
        command.setArguments(queryParams.toString());
        command.setExitCode(0);

        String mockJson = "{\"Filter\":\"*\",\"SearchBase\":\"DC=Domain,DC=ee\"}";
        when(commandWorker.executeCommand(mockCommand, mockJson)).thenReturn(command);

        String url = getBaseUrl() + "/groups?Filter=*&SearchBase=DC=Domain,DC=ee";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(null), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("TestGroup3"));
    }
}
