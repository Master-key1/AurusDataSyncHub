
package com.auruspay.controller;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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
     */
    @GetMapping("/analysis")
    public Map<String, Object> fetchLog(
            @RequestHeader("txnid") String txnid) {

        log.info("Fetching transaction logs for txnId={}", txnid);

        requestDto.setCategory("Switch");
        requestDto.setTxnId(txnid);
        requestDto.setUuid("2e407ae4-1efb-4d6a-b9bb-a7624e74590d");

        try {
            Map<String, Object> response = webClient.post()
                    .uri("http://192.168.180.51:8888/aurus/host/log")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDto)
                    .retrieve()
                    .bodyToMono(
                            new ParameterizedTypeReference<Map<String, Object>>() {}
                    )
                    .block();

            if (response == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Aurus Claw returned an empty response"
                );
            }

            log.info("Aurus Claw request completed for txnId={}", txnid);
            return response;

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
