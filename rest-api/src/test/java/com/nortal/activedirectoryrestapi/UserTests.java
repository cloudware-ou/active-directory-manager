package com.nortal.activedirectoryrestapi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

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

        String url = getBaseUrl() + "/users";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void testUpdateUser() throws Exception {

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
        ResponseEntity<String> updateResponse = restTemplate.exchange(updateUrl, HttpMethod.PUT, updateEntity, String.class);

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        createdUserSamAccountName = "testuserupdate";
    }

    @Test
    public void testGetUsers() {

        String url = getBaseUrl() + "/users?Filter=*&SearchBase=DC=Domain,DC=ee";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(null), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("testuser2"));
    }

}
