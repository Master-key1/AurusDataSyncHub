package com.auruspay.test;

import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExtractCctRequests {

    public static void main(String[] args) throws Exception {

        String filePath = args.length > 0
                ? args[0]
                : "C:\\Users\\nkharose\\Videos\\AurusDataSyncHub\\data\\data.json";

        Path inputPath = Paths.get(filePath);

        String content = Files.readString(inputPath);

        JSONObject root = new JSONObject(content);

        // Output file in same location
        Path outputFile = inputPath.getParent().resolve("processor_request.json");

        StringBuilder output = new StringBuilder();

        for (String key : root.keySet()) {

            Object value = root.get(key);

            if (value instanceof JSONObject) {

                JSONObject entry = (JSONObject) value;

                if (entry.has("processor_request")) {

                    Object cctRequest = entry.get("processor_request");

                    // Write each JSON object in one line
                    if (cctRequest instanceof JSONObject) {
                        output.append(((JSONObject) cctRequest).toString());
                    } else {
                        output.append(String.valueOf(cctRequest));
                    }

                    // New line after each JSON object
                    output.append(System.lineSeparator());
                }
            }
        }

        // Write all JSON objects into one file
        Files.writeString(outputFile, output.toString());

        System.out.println("Created file: " + outputFile);
    }
}