package com.eventledger.event_gateway.model;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

@Converter
public class MetadataConverter implements AttributeConverter<Map<String, Object>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> metadata) {
        if (metadata == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(metadata);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not serialize metadata to JSON", e);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read metadata JSON", e);
        }
    }
}