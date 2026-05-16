package com.asim.qa.utils;

import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonUtils {

    public static String readJson(String fileName) {

        String fullPath = "src/test/resources/test-data/request/" + fileName;

        try {
            return new String(Files.readAllBytes(Paths.get(fullPath)));

        } catch (Exception e) {
            throw new RuntimeException("JSON dosyası okunamadı: " + fullPath);
        }
    }

    public static String updateJsonField(String jsonBody, String path, String value) {

        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();

            com.fasterxml.jackson.databind.JsonNode rootNode =
                    objectMapper.readTree(jsonBody);

            String[] keys = path.split("\\.");

            com.fasterxml.jackson.databind.node.ObjectNode currentNode =
                    (com.fasterxml.jackson.databind.node.ObjectNode) rootNode;

            for (int i = 0; i < keys.length - 1; i++) {

                currentNode =
                        (com.fasterxml.jackson.databind.node.ObjectNode) currentNode.get(keys[i]);
            }

            String finalKey = keys[keys.length - 1];

            if (value.matches("-?\\d+")) {
                currentNode.put(finalKey, Integer.parseInt(value));
            } else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                currentNode.put(finalKey, Boolean.parseBoolean(value));
            } else {
                currentNode.put(finalKey, value);
            }

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);

        } catch (Exception e) {
            throw new RuntimeException("JSON field güncellenemedi: " + path, e);
        }
    }
}
