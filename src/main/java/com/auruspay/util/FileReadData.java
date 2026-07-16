package com.auruspay.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FileReadData {

    private static final Logger log =
            LoggerFactory.getLogger(FileReadData.class);


    private final List<String> KEYWORDS = Arrays.asList(
            "[STPL-GRAY-STREAM]-AURUSPAY ENCRYPTED REQUEST :",
            "[STPL-GRAY-STREAM]-AURUSPAY ENCRYPTED RESPONSE :",
            "[STPL-GRAY-STREAM]-FINAL RESPONSE :",
            "[STPL-GRAY-STREAM]- PROCESSOR REQUEST :",
            "PROCESSOR TERMINAL DETAILS"
    );


    private static final Pattern UUID_PATTERN =
            Pattern.compile(
                    "[0-9a-fA-F]{8}-" +
                    "[0-9a-fA-F]{4}-" +
                    "[0-9a-fA-F]{4}-" +
                    "[0-9a-fA-F]{4}-" +
                    "[0-9a-fA-F]{12}"
            );


    private final Pattern FIELD_PATTERN =
            Pattern.compile("(\\w+)\\s*=\\s*([^,\\]]+)");



    /**
     * Find UUID from line containing
     * Generated Aurus Transaction ID
     *
     * @param filePath absolute or relative path to the log file to search
     */
    public String findUUID(String filePath) throws IOException {

        String searchText =
                "Generated Aurus Transaction ID";

        log.info("Searching Generated Aurus Transaction ID in file : {}", filePath);

        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {

            log.error("Input file not found at path : {}", filePath);

            return null;
        }

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     new FileInputStream(path.toFile())))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.contains(searchText)) {

                    Matcher matcher =
                            UUID_PATTERN.matcher(line);

                    if (matcher.find()) {

                        String uuid = matcher.group();

                        log.info(
                                "Aurus Transaction UUID found : {}",
                                uuid
                        );

                        return uuid;
                    }
                }
            }

        } catch (Exception e) {

            log.error(
                    "Error while finding UUID",
                    e
            );

            throw e;
        }

        log.warn(
                "No UUID found for Generated Aurus Transaction ID"
        );

        return null;
    }





    /**
     * Extract keyword based data
     *
     * @param filePath absolute or relative path to the log file to read
     */
    public Map<String, List<String>> extractData(String filePath)
            throws IOException {

        Map<String, List<String>> resultMap =
                new LinkedHashMap<>();

        String uuid = findUUID(filePath);

        if (uuid == null) {

            log.warn("UUID not found. Skipping extraction");

            return resultMap;
        }

        log.info(
                "Searching data for UUID : {}",
                uuid
        );

        File file = new File(filePath);

        log.info(
                "Reading file : {}",
                file.getAbsolutePath()
        );

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (!line.contains(uuid)) {

                    continue;
                }

                log.debug(
                        "UUID matched line : {}",
                        line
                );

                for (String keyword : KEYWORDS) {

                    if (line.contains(keyword)) {

                        String extractedData =
                                extractValue(
                                        line,
                                        keyword
                                );

                        log.info(
                                "Keyword : {} Value : {}",
                                keyword,
                                extractedData
                        );

                        resultMap
                        .computeIfAbsent(
                                keyword,
                                k -> new ArrayList<>()
                        )
                        .add(extractedData);

                        break;
                    }
                }
            }

        } catch (Exception e) {

            log.error(
                    "Error while extracting data",
                    e
            );
        }

        log.info(
                "Extraction completed. Keys found : {}",
                resultMap.keySet()
        );

        return resultMap;
    }





    /**
     * Extract value after keyword
     */
    private String extractValue(
            String line,
            String keyword) {

        if (keyword.equals("PROCESSOR TERMINAL DETAILS")) {

            Matcher matcher =
                    FIELD_PATTERN.matcher(line);

            while (matcher.find()) {

                String fieldName =
                        matcher.group(1);

                String fieldValue =
                        matcher.group(2).trim();

                if ("processorId".equals(fieldName)) {

                    return fieldValue;
                }
            }

            return "";
        }

        return line.substring(
                line.indexOf(keyword)
                        + keyword.length()
        ).trim();
    }





    /**
     * Scans a directory and builds the dynamic list of file paths to
     * process, e.g. the folder containing files like:
     *   Ann Taylor_credit_keyed_Avs.txt
     *   At Home_Credit_keyed_Avs.txt
     *   Express_Credit_AVS_Keyed.txt
     *
     * @param directoryPath path to the folder containing the log files
     * @param extension     file extension filter, e.g. ".txt"
     *                      (pass null or "" to include all files)
     */
    public List<String> listFilesInDirectory(
            String directoryPath,
            String extension) {

        List<String> filePaths = new ArrayList<>();

        File dir = new File(directoryPath);

        if (!dir.exists() || !dir.isDirectory()) {

            log.error(
                    "Directory not found or not a directory : {}",
                    directoryPath
            );

            return filePaths;
        }

        File[] files = dir.listFiles();

        if (files == null || files.length == 0) {

            log.warn(
                    "No files found in directory : {}",
                    directoryPath
            );

            return filePaths;
        }

        for (File file : files) {

            if (!file.isFile()) {

                continue;
            }

            if (extension != null
                    && !extension.isEmpty()
                    && !file.getName().toLowerCase()
                            .endsWith(extension.toLowerCase())) {

                continue;
            }

            filePaths.add(file.getAbsolutePath());
        }

        log.info(
                "Found {} file(s) in directory : {}",
                filePaths.size(),
                directoryPath
        );

        return filePaths;
    }





    /**
     * Process a dynamic list of file paths, one after another.
     * Returns a map keyed by file path, each value being the
     * keyword -> extracted values map for that file.
     *
     * @param filePaths dynamic list of log file paths to process
     */
    public Map<String, Map<String, List<String>>> extractDataForFiles(
            List<String> filePaths) {

        Map<String, Map<String, List<String>>> allResults =
                new LinkedHashMap<>();

        if (filePaths == null || filePaths.isEmpty()) {

            log.warn("No file paths provided. Nothing to process");

            return allResults;
        }

        log.info(
                "Processing {} file(s)",
                filePaths.size()
        );

        for (String filePath : filePaths) {

            try {

                log.info(
                        "---------- Processing file : {} ----------",
                        filePath
                );

                Map<String, List<String>> data =
                        extractData(filePath);

                allResults.put(filePath, data);

            } catch (IOException e) {

                log.error(
                        "Error while processing file : {}",
                        filePath,
                        e
                );

                allResults.put(filePath, Collections.emptyMap());
            }
        }

        return allResults;
    }





    public static void main(String[] args)
            throws Exception {

        // Pass the folder containing the log files as a command-line
        // argument, e.g.:
        //   java ExtractMultipleKeywords "D:\Logs\AnnTaylor"
        // The dynamic file list (all .txt files in that folder, such as
        // "Ann Taylor_credit_keyed_Avs.txt", "At Home_Credit_keyed_Avs.txt",
        // "Express_Credit_AVS_Keyed.txt", etc.) is built automatically.
        // If no argument is given, it falls back to the current directory.
		String directoryPath = (args.length > 0) ? args[0] : "C:\\Users\\nkharose\\Pictures\\Data\\FD\\combine";

		FileReadData extractor = new FileReadData();

		List<String> filePaths = extractor.listFilesInDirectory(directoryPath, ".txt");

		if (filePaths.isEmpty()) {

			log.info("No .txt files found to process");

			return;
		}

		Map<String, Map<String, List<String>>> allResults = extractor.extractDataForFiles(filePaths);

		 if (allResults.isEmpty()) {

            log.info(
                    "No matching data found"
            );

            return;
        }

        allResults.forEach((filePath, data) -> {

            log.info(
                    "========== FILE : {} ==========",
                    filePath
            );

            if (data.isEmpty()) {

                log.info("No matching data found for this file");

                return;
            }

            data.forEach((key, values) -> {

                log.info(
                        "---------- *{} ----------",
                        key
                );

                values.forEach(value ->
                        log.info("value* : {}",value)
                );

            });
        });
    }
}