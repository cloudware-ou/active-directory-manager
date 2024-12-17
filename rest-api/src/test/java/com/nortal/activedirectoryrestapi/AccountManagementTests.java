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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountManagementTests {

    @LocalServerPort
    private int port;

    @Mock
    private CommandWorker commandWorker;

    @Mock
    private CommandService commandService;

    private TestRestTemplate restTemplate;

    private String createdUserSamAccountName;

    Helper helper = new Helper();

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    public void setUp() throws Exception {
        helper.createTestUser(getBaseUrl() + "/users", commandWorker);
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
    public void testChangePassword() throws ADCommandExecutionException, InterruptedException {
        String payload = "{" +
                "\"Identity\": \"testuser\"," +
                "\"NewPassword\": \"NewP@ssw0rd123!\"," +
                "\"OldPassword\": \"ComplexP@ssw0rd4567\"" +
                "}";

        Command mockCommand = new Command();
        mockCommand.setCommand("Set-ADAccountPassword");
        mockCommand.setArguments(payload);
        mockCommand.setExitCode(0);
        mockCommand.setId(9L);

        when(commandWorker.executeCommand("Set-ADAccountPassword", payload)).thenReturn(mockCommand);
        when(commandService.getCommand(mockCommand.getId())).thenReturn(mockCommand);

        String url = getBaseUrl() + "/accounts/password";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Command savedCommand = commandService.getCommand(mockCommand.getId());
        assertNotNull(savedCommand);
        assertEquals("Set-ADAccountPassword", savedCommand.getCommand());
        assertEquals(payload, savedCommand.getArguments());
        assertEquals(0, savedCommand.getExitCode());
    }

    @Test
    public void testEnableAccount() throws ADCommandExecutionException, InterruptedException {
        String payload = "{" +
                "\"Identity\": \"testuser\"" +
                "}";

        Command mockCommand = new Command();
        mockCommand.setCommand("Enable-ADAccount");
        mockCommand.setArguments(payload);
        mockCommand.setExitCode(0);
        mockCommand.setId(2L);

        when(commandWorker.executeCommand("Enable-ADAccount", payload)).thenReturn(mockCommand);
        when(commandService.getCommand(mockCommand.getId())).thenReturn(mockCommand);

        String url = getBaseUrl() + "/accounts/enable";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Command savedCommand = commandService.getCommand(mockCommand.getId());
        assertNotNull(savedCommand);
        assertEquals("Enable-ADAccount", savedCommand.getCommand());
        assertEquals(payload, savedCommand.getArguments());
        assertEquals(0, savedCommand.getExitCode());
    }

    @Test
    public void testDisableAccount() throws ADCommandExecutionException, InterruptedException {
        String payload = "{" +
                "\"Identity\": \"testuser\"" +
                "}";

        Command mockCommand = new Command();
        mockCommand.setCommand("Disable-ADAccount");
        mockCommand.setArguments(payload);
        mockCommand.setExitCode(0);
        mockCommand.setId(3L);

        when(commandWorker.executeCommand("Disable-ADAccount", payload)).thenReturn(mockCommand);
        when(commandService.getCommand(mockCommand.getId())).thenReturn(mockCommand);

        String url = getBaseUrl() + "/accounts/disable";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Command savedCommand = commandService.getCommand(mockCommand.getId());
        assertNotNull(savedCommand);
        assertEquals("Disable-ADAccount", savedCommand.getCommand());
        assertEquals(payload, savedCommand.getArguments());
        assertEquals(0, savedCommand.getExitCode());
    }
}
