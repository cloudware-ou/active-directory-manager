package com.nortal.activedirectoryrestapi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountManagementTests {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;

    private String createdUserSamAccountName;

    Helper helper = new Helper();

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    public void setUp() throws Exception {
        helper.createTestUser(getBaseUrl() + "/users");
        restTemplate = new TestRestTemplate();
        createdUserSamAccountName = "testuser";
    }

    @AfterEach
    public void tearDown() {
        if (createdUserSamAccountName != null) {
            helper.deleteIfExists(createdUserSamAccountName, getBaseUrl()+"/users");
        }
    }

    @Test
    public void testChangePassword() {
        String payload = "{" +
                "\"Identity\": \"testuser\"," +
                "\"NewPassword\": \"NewP@ssw0rd123!\"," +
                "\"OldPassword\": \"ComplexP@ssw0rd4567\"" +
                "}";

        String url = getBaseUrl() + "/accounts/password";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    public void testEnableAccount() {
        String payload = "{" +
                "\"Identity\": \"testuser\"" +
                "}";


        String url = getBaseUrl() + "/accounts/enable";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    public void testDisableAccount() {
        String payload = "{" +
                "\"Identity\": \"testuser\"" +
                "}";


        String url = getBaseUrl() + "/accounts/disable";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());


    }
}
