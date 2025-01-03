package com.nortal.activedirectoryrestapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Component
public class Helper {

    @Autowired
    private TestRestTemplate restTemplate = new TestRestTemplate();

    public void deleteIfExists(String identity, String baseUrl) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("Identity", identity);

        HttpEntity<MultiValueMap<String, Object>> deleteEntity = new HttpEntity<>(params);

        restTemplate.exchange(
                baseUrl,
                HttpMethod.DELETE,
                deleteEntity,
                String.class
        );
    }

    public void createTestUser(String baseUrl) {
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


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> createResponse = restTemplate.exchange(baseUrl, HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

    }

    public void createGroup(String baseUrl) {
        String payload = "{" +
                "\"Name\": \"TestGroup2\"," +
                "\"GroupScope\": \"Global\"," +
                "\"GroupCategory\": \"Security\"" +
                "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }


    public void addMemberToGroup(String groupName, String memberSamAccountName, String baseUrl) {
        String payload = "{"
                + "\"Identity\": \"" + groupName + "\","
                + "\"Members\": \"" + memberSamAccountName + "\""
                + "}";

        String url = baseUrl + "/groups/members";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }


}
