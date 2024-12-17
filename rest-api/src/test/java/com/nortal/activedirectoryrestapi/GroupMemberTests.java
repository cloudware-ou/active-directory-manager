package com.nortal.activedirectoryrestapi;

import com.nortal.activedirectoryrestapi.controllers.RESTApiController;
import com.nortal.activedirectoryrestapi.entities.Command;
import com.nortal.activedirectoryrestapi.exceptions.ADCommandExecutionException;
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
public class GroupMemberTests {

    @LocalServerPort
    private int port;

    @InjectMocks
    private RESTApiController restApiController;

    @Mock
    private CommandWorker commandWorker;

    @Mock
    private CommandService commandService;

    private TestRestTemplate restTemplate;

    Helper helper = new Helper();



    private String groupName = "testGroup2";
    private String memberSamAccountName = "testuser";


    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    public void setUp() throws Exception {
        restTemplate = new TestRestTemplate();
        helper.createTestUser(getBaseUrl()+"/users", commandWorker);
        helper.createGroup(getBaseUrl()+"/groups", commandWorker);
        helper.addMemberToGroup(groupName, memberSamAccountName, commandWorker, getBaseUrl());

    }

    @AfterEach
    public void tearDown() {
        if (memberSamAccountName != null && groupName != null) {
            helper.deleteIfExists(groupName, getBaseUrl()+"/groups");
            helper.deleteIfExists(memberSamAccountName, getBaseUrl()+"/users");

        }
    }


    @Test
    public void testGetGroupMembers() throws Exception {

        MultiValueMap<String, Object> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("Identity", "CN=gruppp,CN=Users,DC=Domain,DC=ee");

        String mockCommand = "Get-ADGroupMember";
        Command command = new Command();
        command.setCommand(mockCommand);
        command.setArguments(queryParams.toString());

        String mockJson = "{\"Identity\":\"CN=gruppp,CN=Users,DC=Domain,DC=ee\"}";
        when(commandWorker.executeCommand(mockCommand, mockJson)).thenReturn(command);

        String url = getBaseUrl() + "/groups/members?Identity=CN=gruppp,CN=Users,DC=Domain,DC=ee";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(null), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        System.out.println(response.getBody());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("Arkadi Statsenko"));


    }


    @Test
    public void testAddMemberToGroup() throws Exception {
        String payload = "{"
                + "\"Identity\": \"" + groupName + "\","
                + "\"Members\": \"" + memberSamAccountName + "\""
                + "}";

        Command mockCommand = new Command();
        mockCommand.setCommand("Add-ADGroupMember");
        mockCommand.setArguments(payload);

        when(commandWorker.executeCommand("Add-ADGroupMember", payload)).thenReturn(mockCommand);
        when(commandService.getCommand(mockCommand.getId())).thenReturn(mockCommand);

        String url = getBaseUrl() + "/groups/members";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Command savedCommand = commandService.getCommand(mockCommand.getId());
        assertNotNull(savedCommand);
        assertEquals("Add-ADGroupMember", savedCommand.getCommand());
        assertEquals(payload, savedCommand.getArguments());
        assertEquals(0, savedCommand.getExitCode());

        groupName = "TestGroup2";
        memberSamAccountName = "testuser";

    }

    @Test
    public void testRemoveMemberFromGroup() throws Exception {

        String deleteUrl = getBaseUrl() + "/groups/members";
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("Identity", groupName);
        params.add("Members", memberSamAccountName);

        HttpEntity<MultiValueMap<String, Object>> deleteEntity = new HttpEntity<>(params);

        ResponseEntity<String> response = restTemplate.exchange(
                deleteUrl,
                HttpMethod.DELETE,
                deleteEntity,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        groupName = "TestGroup2";
        memberSamAccountName = "testuser";

    }
}
