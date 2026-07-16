package com.auruspay.util;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class ExtractMultipleKeywords {

    private static final Logger log =
            LoggerFactory.getLogger(ExtractMultipleKeywords.class);


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
     */
    public String findUUID() throws IOException {


        String searchText =
                "Generated Aurus Transaction ID";


        log.info("Searching Generated Aurus Transaction ID");


        InputStream inputStream =
                getClass()
                .getClassLoader()
                .getResourceAsStream("input.txt");


        if (inputStream == null) {

            log.error("input.txt not found");

            return null;
        }


        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(inputStream))) {


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
     */
    public Map<String, List<String>> extractData()
            throws IOException {


        Map<String, List<String>> resultMap =
                new LinkedHashMap<>();


        String uuid = findUUID();


        if (uuid == null) {

            log.warn("UUID not found. Skipping extraction");

            return resultMap;
        }


        log.info(
                "Searching data for UUID : {}",
                uuid
        );



        File file =
                new ClassPathResource("input.txt")
                        .getFile();



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





    public static void main1(String[] args)
            throws Exception {


        ExtractMultipleKeywords extractor =
                new ExtractMultipleKeywords();



        Map<String, List<String>> data =
                extractor.extractData();



        if (data.isEmpty()) {


            log.info(
                    "No matching data found"
            );


            return;
        }



        data.forEach((key, values) -> {


            log.info(
                    "========== {} ==========",
                    key
            );


            values.forEach(value ->
                    log.info(value)
            );

        });
    }
}