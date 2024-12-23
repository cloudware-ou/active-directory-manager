package com.nortal.activedirectoryrestapi.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JSONHandler {

    private final CryptoService cryptoService;

    public JsonNode convertToJson(MultiValueMap<String, Object> multiValueMap) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.valueToTree(
                multiValueMap.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().size() == 1 ? entry.getValue().getFirst() : entry.getValue()
                        )));
    }

    public void secureJson(JsonNode payload) throws Exception {

        List<String> fieldsToEncrypt = List.of("AccountPassword", "NewPassword", "OldPassword");
        for (String field : fieldsToEncrypt) {
            if (payload.has(field)) {
                cryptoService.exchangeKeys();
                String[] cipher = cryptoService.encrypt(payload.path(field).asText());
                if (payload.isObject()) {
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode objectNode = (ObjectNode) payload;
                    ObjectNode nestedObject = mapper.createObjectNode();
                    nestedObject.put("iv", cipher[0]);
                    nestedObject.put("ciphertext", cipher[1]);
                    objectNode.set(field, nestedObject);
                }
            }
        }
        cryptoService.eraseSharedSecret();
    }
}
