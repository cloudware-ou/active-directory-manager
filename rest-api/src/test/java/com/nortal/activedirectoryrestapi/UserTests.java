package com.nortal.activedirectoryrestapi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserTests {

    @LocalServerPort
    private int port;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    private TestRestTemplate restTemplate;

    private String createdUserSamAccountName;

    private final Helper helper = new Helper();

    private final String newUserPayload = "{"
            + "\"Name\": \"Test User\","
            + "\"GivenName\": \"Test\","
            + "\"Surname\": \"User\","
            + "\"SamAccountName\": \"testuser\","
            + "\"UserPrincipalName\": \"testuser@domain.com\","
            + "\"Path\": \"CN=Users,DC=Domain,DC=ee\","
            + "\"Enabled\": true,"
            + "\"AccountPassword\": \"ComplexP@ssw0rd4567\""
            + "}";


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
    public void testCreateNewUser() {

        createdUserSamAccountName = "testuser";

        String url = getBaseUrl() + "/users";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(newUserPayload, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void testUpdateUser() {

        helper.createTestUser(getBaseUrl()+"/users");

        String updatePayload = "{"
                + "\"Identity\": \"testuser\","
                + "\"GivenName\": \"Test\","
                + "\"Surname\": \"User\","
                + "\"SamAccountName\": \"testuserupdate\","
                + "\"UserPrincipalName\": \"testuser@domain.com\","
                + "\"Enabled\": true"
                + "}";

        String updateUrl = getBaseUrl() + "/users";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> updateEntity = new HttpEntity<>(updatePayload, headers);

        ResponseEntity<JsonNode> updateResponse = restTemplate.exchange(updateUrl, HttpMethod.PATCH, updateEntity, JsonNode.class);

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        createdUserSamAccountName = "testuserupdate";
    }

    @Test
    public void testCreateTheSameUserAgain() {
        helper.createTestUser(getBaseUrl()+"/users");
        String url = getBaseUrl() + "/users";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(newUserPayload, headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        createdUserSamAccountName = "testuser";
    }

    @Test
    public void testGetUsers1() {

        String url = getBaseUrl() + "/users?Filter=*&SearchBase=DC=Domain,DC=ee";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).toPrettyString().contains("testuser2"));
    }

    @Test
    public void testGetUsers2() {

        String url = getBaseUrl() + "/users?Identity=nonexistinguser";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

}
