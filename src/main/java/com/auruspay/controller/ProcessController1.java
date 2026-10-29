
package com.auruspay.controller;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.auruspay.dto.ProcessRequest;
import com.auruspay.dto.RequestDto;
import com.auruspay.service.JsonDataAddService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class ProcessController1 {

    private static final Logger log =
            LoggerFactory.getLogger(ProcessController1.class);

    private final WebClient webClient;
    private final RestTemplate restTemplate;
    private final JsonDataAddService jsonDataAddService;
    private final RequestDto requestDto;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${aurus.claw.api-key}")
    private String apiKey;

    @Value("${aurus.claw.forwarded-for:ip_address}")
    private String forwardedFor;

    private static final String CLAW_URL =
            "http://192.168.180.51:8888/";

    private static final String HOST_LOG_URL =
            "http://192.168.50.177:9090/aurus/host/log";

    public ProcessController1(
            WebClient.Builder webClientBuilder,
            RestTemplate restTemplate,
            JsonDataAddService jsonDataAddService,
            RequestDto requestDto) {

        this.webClient = webClientBuilder
                .build();

        this.restTemplate = restTemplate;
        this.jsonDataAddService = jsonDataAddService;
        this.requestDto = requestDto;
    }

    /**
     * Fetch transaction logs from Aurus Claw.
     *
     * Example:
     * GET /analysis
     * Header: txnid: 292263002940953034
     * @throws JsonProcessingException 
     */
    @GetMapping("/analysis")
    public Map<String, Object> fetchLog(
            @RequestHeader("txnid") String txnid) throws JsonProcessingException {

        log.info("Fetching transaction logs for txnId={}", txnid);

        requestDto.setCategory("Switch");
        requestDto.setTxnId(txnid);
        requestDto.setUuid("2e407ae4-1efb-4d6a-b9bb-a7624e74590d");

        try {
        	String response = webClient.post()
        	        .uri("http://192.168.180.51:8888/aurus/host/log")
        	        .contentType(MediaType.APPLICATION_JSON)
        	        .accept(MediaType.APPLICATION_JSON)
        	        .bodyValue(requestDto)
        	        .retrieve()
        	        .bodyToMono(String.class)
        	        .block();

            if (response == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Aurus Claw returned an empty response"
                );
            }
            Map<String, Object>   resp =  mapTransactionLogs(response);


            log.info("Aurus Claw request completed for txnId={}", txnid);
            return resp;

        } catch (WebClientResponseException e) {
            log.error("Aurus Claw returned HTTP status {} for txnId={}",
                    e.getStatusCode().value(), txnid);

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Aurus Claw request failed with HTTP status "
                            + e.getStatusCode().value()
            );

        } catch (WebClientRequestException e) {
            log.error("Unable to connect to Aurus Claw for txnId={}: {}",
                    txnid, e.getMessage());

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to connect to Aurus Claw. Check network configuration."
            );
        }
        
           }
    
    //public ObjectNode mapTransactionLogs(String responseJson) throws JsonProcessingException {
    	
    	public Map<String, Object> mapTransactionLogs(String responseJson)
            throws JsonProcessingException {

        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode data = root.path("data");

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("status", root.path("status").asText());
        result.put("message", root.path("message").asText());
        result.put("details", objectMapper.convertValue(
                data.path("details"), Map.class));

        Map<String, Object> transactionSummary = new LinkedHashMap<>();
        Map<String, Object> logs = new LinkedHashMap<>();

        List<String> emvTags = new ArrayList<>();
        List<String> productData = new ArrayList<>();
        List<String> aurusPayLogs = new ArrayList<>();
        List<String> originalLines = new ArrayList<>();

        String currentSection = "";

        for (JsonNode lineNode : data.path("lines")) {
            String line = lineNode.asText().trim();
            originalLines.add(line);

            if (line.isEmpty()) {
                continue;
            }

            if (line.contains("TRANSACTION SUMMARY")) {
                currentSection = "TRANSACTION_SUMMARY";
                continue;
            } else if (line.startsWith("CCT REQUEST:")) {
                currentSection = "CCT_REQUEST";
                continue;
            } else if (line.startsWith("CCT RESPONSE:")) {
                currentSection = "CCT_RESPONSE";
                continue;
            } else if (line.startsWith("PROCESSOR REQUEST:")) {
                currentSection = "PROCESSOR_REQUEST";
                continue;
            } else if (line.startsWith("PROCESSOR RESPONSE:")) {
                currentSection = "PROCESSOR_RESPONSE";
                continue;
            } else if (line.contains("EMV TAGS")) {
                currentSection = "EMV_TAGS";
                continue;
            } else if (line.contains("PRODUCT DATA")) {
                currentSection = "PRODUCT_DATA";
                continue;
            } else if (line.contains("AURUSPAY LOG")) {
                currentSection = "AURUSPAY_LOG";
                continue;
            }

            switch (currentSection) {
                case "TRANSACTION_SUMMARY":
                    int separator = line.indexOf(':');

                    if (separator > 0) {
                        String key = line.substring(0, separator).trim();
                        String value = line.substring(separator + 1).trim();

                        transactionSummary.put(toCamelCase(key), value);
                    }
                    break;

                case "CCT_REQUEST":
                case "CCT_RESPONSE":
                case "PROCESSOR_REQUEST":
                case "PROCESSOR_RESPONSE":
                    if (line.startsWith("{")) {
                        try {
                            logs.put(currentSection,
                                    objectMapper.readValue(line, Map.class));
                            logs.put(currentSection + "Status", "FOUND");
                        } catch (JsonProcessingException e) {
                            logs.put(currentSection + "Raw", line);
                            logs.put(currentSection + "Status", "INVALID_JSON");
                        }
                    } else if (line.contains("not found in this log excerpt")) {
                        logs.put(currentSection, null);
                        logs.put(currentSection + "Status", "NOT_FOUND");
                    }
                    break;

                case "EMV_TAGS":
                    emvTags.add(line);
                    break;

                case "PRODUCT_DATA":
                    productData.add(line);
                    break;

                case "AURUSPAY_LOG":
                    aurusPayLogs.add(line);
                    break;

                default:
                    break;
            }
        }

        result.put("transactionSummary", transactionSummary);
        result.put("logs", logs);
        result.put("emvTags", emvTags);
        result.put("productData", productData);
        result.put("aurusPayLogs", aurusPayLogs);
        result.put("originalLines", originalLines);

        return result;
    }
    	private String toCamelCase(String key) {
    	    String[] parts = key.trim().split("[\\s_:-]+");
    	    StringBuilder result = new StringBuilder(parts[0].toLowerCase());

    	    for (int i = 1; i < parts.length; i++) {
    	        if (!parts[i].isEmpty()) {
    	            result.append(parts[i].substring(0, 1).toUpperCase())
    	                  .append(parts[i].substring(1).toLowerCase());
    	        }
    	    }

    	    return result.toString();
    	}

    /**
     * Save transaction data.
     *
     * Keep your existing CCT request and response values here.
     */
    @PostMapping(
            value = "/processing",
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> saveTransaction() {

        String cctReq = "REPLACE_WITH_YOUR_EXISTING_CCT_REQUEST";
        String cctResp = "REPLACE_WITH_YOUR_EXISTING_CCT_RESPONSE";

        ProcessRequest request = new ProcessRequest();
        request.setCctRequest(cctReq);
        request.setCctResponse(cctResp);
        request.setProcessorId("00");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.ALL));

            Map<String, String> fetchLogRequest = new HashMap<>();

            // Supply the actual transaction ID when required
            // by the target host-log service.
            fetchLogRequest.put("txnid", null);
            fetchLogRequest.put(
                    "uuid",
                    "86e278a3-5349-4eb6-b7e5-9a45ae6306b9"
            );

            org.springframework.http.HttpEntity<Map<String, String>>
                    entity = new org.springframework.http.HttpEntity<>(
                            fetchLogRequest,
                            headers
                    );

            // Retains the original host-log endpoint and allows
            // RestClientException handling for this call.
            ResponseEntity<String> targetResponse =
                    restTemplate.postForEntity(
                            HOST_LOG_URL,
                            entity,
                            String.class
                    );

            log.info(
                    "Host-log service returned HTTP status {}",
                    targetResponse.getStatusCode().value()
            );

            String transactionId =
                    jsonDataAddService.saveData(request);

            log.info(
                    "Transaction saved successfully. txnId={}",
                    transactionId
            );

            return ResponseEntity.ok(transactionId);

        } catch (org.springframework.web.client.HttpStatusCodeException e) {

            log.error(
                    "Host-log service returned HTTP status {}",
                    e.getStatusCode().value()
            );

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body("Target microservice request failed");

        } catch (RestClientException e) {

            log.error("Unable to call target microservice", e);

            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body("Unable to call target microservice");

        } catch (Exception e) {

            log.error("Error processing transaction", e);

            return ResponseEntity
                    .internalServerError()
                    .body("Failed to process transaction");
        }
    }

    /**
     * Decode Base64-encoded log content.
     */
    public String decryptLogs(String encodedLogs) {

        if (encodedLogs == null || encodedLogs.isBlank()) {
            return "";
        }

        try {
            String encoded = encodedLogs.trim();

            if (encoded.startsWith("{")) {
                JsonNode root =
                        new ObjectMapper().readTree(encoded);

                encoded = root.path("message").asText("");

                if (encoded.isBlank()) {
                    throw new IllegalArgumentException(
                            "Message field is missing or empty"
                    );
                }
            }

            encoded = encoded.replaceAll("\\s+", "");

            byte[] decodedBytes =
                    Base64.getDecoder().decode(encoded);

            return new String(
                    decodedBytes,
                    StandardCharsets.UTF_8
            );

        } catch (IllegalArgumentException e) {

            log.debug("Response is not valid Base64");

            return encodedLogs;

        } catch (Exception e) {

            log.warn(
                    "Unable to decode log response: {}",
                    e.getClass().getSimpleName()
            );

            return "Invalid Base64 content. Check the input format.";
        }
    }
}
