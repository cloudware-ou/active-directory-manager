package com.nortal.activedirectoryrestapi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GroupMemberTests {

    @LocalServerPort
    private int port;

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
        helper.createTestUser(getBaseUrl()+"/users");
        helper.createGroup(getBaseUrl()+"/groups");
        helper.addMemberToGroup(groupName, memberSamAccountName, getBaseUrl());

    }

    @AfterEach
    public void tearDown() {
        if (memberSamAccountName != null && groupName != null) {
            helper.deleteIfExists(groupName, getBaseUrl()+"/groups");
            helper.deleteIfExists(memberSamAccountName, getBaseUrl()+"/users");

        }
    }


    @Test
    public void testGetGroupMembers() {

        String url = getBaseUrl() + "/groups/members?Identity=CN=gruppp,CN=Users,DC=Domain,DC=ee";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        System.out.println(response.getBody());
        assertTrue(Objects.requireNonNull(response.getBody()).toPrettyString().contains("Arkadi Statsenko"));


    }


    @Test
    public void testAddMemberToGroup() {
        String payload = "{"
                + "\"Identity\": \"" + groupName + "\","
                + "\"Members\": \"" + memberSamAccountName + "\""
                + "}";


        String url = getBaseUrl() + "/groups/members";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());


        groupName = "TestGroup2";
        memberSamAccountName = "testuser";

    }

    @Test
    public void testRemoveMemberFromGroup() {

        String deleteUrl = getBaseUrl() + "/groups/members";
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("Identity", groupName);
        params.add("Members", memberSamAccountName);

        HttpEntity<MultiValueMap<String, Object>> deleteEntity = new HttpEntity<>(params);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                deleteUrl,
                HttpMethod.DELETE,
                deleteEntity,
                JsonNode.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        groupName = "TestGroup2";
        memberSamAccountName = "testuser";

    }
}
