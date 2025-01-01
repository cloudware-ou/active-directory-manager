package com.nortal.activedirectoryrestapi.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;


import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JSONHandler {

    /**
     * Converts MultiValueMap to JSON
     * @param multiValueMap MultiValueMap object
     * @return JSON object
     */
    public JsonNode convertToJson(MultiValueMap<String, Object> multiValueMap) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.valueToTree(
                multiValueMap.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().size() == 1 ? entry.getValue().getFirst() : entry.getValue()
                        )));
    }

    /**
     * Converts Map to JSON
     * @param map Map object
     * @return JSON object
     */
    public JsonNode convertToJson(Map<String, Object> map) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.valueToTree(map);
    }
}
