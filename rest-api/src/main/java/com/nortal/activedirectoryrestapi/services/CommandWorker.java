package com.nortal.activedirectoryrestapi.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.nortal.activedirectoryrestapi.entities.Command;
import com.nortal.activedirectoryrestapi.exceptions.ADCommandExecutionException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommandWorker {
    private final CommandService commandService;
    private final JSONHandler jsonHandler;
    private final ErrorHandler errorHandler;
    private final NotificationListener notificationListener;
    private final CryptoService cryptoService;

    public Command waitForResult(Long id) {
        return notificationListener.getCompletedCommand(id);
    }

    public Command executeCommand(String command, JsonNode payload) throws ADCommandExecutionException {
        Long id = commandService.saveCommand(command, payload);
        Command entity = this.waitForResult(id);
        if (entity.getExitCode() == 0){
            return entity;
        } else {
            throw new ADCommandExecutionException(entity.getCommand(), entity.getResult(), entity.getExitCode(), entity.getTimestamp());
        }
    }

    public ResponseEntity<JsonNode> submitJob(String command, JsonNode payload, HttpStatusCode httpStatusCode) {
        try{
            return ResponseEntity.status(httpStatusCode).body(this.executeCommand(command, payload).getResult());
        } catch (ADCommandExecutionException e) {
            return errorHandler.createErrorResponse(e);
        }
    }

    public ResponseEntity<JsonNode> submitJob(String command, Map<String, Object> payload, HttpStatusCode httpStatusCode) {
        this.encryptPasswordFields(payload);
        return submitJob(command, jsonHandler.convertToJson(payload), httpStatusCode);
    }

    public ResponseEntity<JsonNode> submitJob(String command, Map<String, Object> payload){
        return submitJob(command, payload, HttpStatus.OK);
    }

    public ResponseEntity<JsonNode> submitJob(String command, MultiValueMap<String, Object> params){
        JsonNode json = jsonHandler.convertToJson(params);
        return submitJob(command, json, HttpStatus.OK);
    }

    /**
     * Converts UTF-8 char array to byte array
     * @param c UTF-8 char array
     * @return byte array
     */
    public byte[] convertCharArrayToByteArray(char[] c) {
        CharBuffer charBuffer = CharBuffer.wrap(c);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        charBuffer.clear();
        byteBuffer.clear();
        return bytes;
    }

    /**
     * Encrypts selected fields of Map, initiates DH Key Exchange
     * @param payload Map, part of which should be encrypted
     */
    public void encryptPasswordFields(Map<String, Object> payload) {

        String[] fieldsToEncrypt = new String[]{"AccountPassword", "NewPassword", "OldPassword"};
        for (String field : fieldsToEncrypt) {
            if (payload.containsKey(field)) {
                if (!cryptoService.hasValidSharedSecret()) cryptoService.exchangeKeys(); // There may be multiple fields to encrypt. If shared secret already exist, use it.

                char[] password_chars = (char[]) payload.get(field);
                byte[] password_bytes = convertCharArrayToByteArray(password_chars);
                cryptoService.securelyEraseCharArray(password_chars);

                String[] cipher = cryptoService.encrypt(password_bytes);
                cryptoService.securelyEraseByteArray(password_bytes);

                payload.put(field, Map.of("iv", cipher[0], "ciphertext", cipher[1]));

            }
        }
        if (cryptoService.hasValidSharedSecret()) cryptoService.eraseSharedSecret(); // If shared secret was created, securely erase it.

    }



}
