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
public class GroupTests {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;

    private String createdGroupName;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }
    private final Helper helper = new Helper();

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
    public void testCreateNewGroup() {
        String payload = "{" +
                "\"Name\": \"TestGroup1\"," +
                "\"GroupScope\": \"Global\"," +
                "\"GroupCategory\": \"Security\"" +
                "}";

        createdGroupName = "TestGroup1";

        String url = getBaseUrl() + "/groups";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

    }

    @Test
    public void testUpdateGroup() throws Exception {
        helper.createGroup(getBaseUrl()+"/groups");

        String updatePayload = "{" +
                "\"Identity\": \"TestGroup2\"," +
                "\"SamAccountName\": \"UpdatedTestGroup\"," +
                "\"GroupScope\": \"Global\"" +
                "}";

        createdGroupName = "TestGroup2";

        String updateUrl = getBaseUrl() + "/groups";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> updateEntity = new HttpEntity<>(updatePayload, headers);
        ResponseEntity<String> updateResponse = restTemplate.exchange(updateUrl, HttpMethod.PATCH, updateEntity, String.class);

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

    }

    @Test
    public void testGetGroups() {
        String url = getBaseUrl() + "/groups?Filter=*&SearchBase=DC=Domain,DC=ee";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("TestGroup3"));
    }
}
