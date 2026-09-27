package org.mvs.talons.talonchecker.mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper()
            // ігнорувати невідомі властивості
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            // підтримка java.time.* класів
            .registerModule(new JavaTimeModule())
            // щоб LocalDateTime серіалізувався/десеріалізувався нормально
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }
}
