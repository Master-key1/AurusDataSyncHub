package com.auruspay.controller;

import com.auruspay.comparator.CctComparator;
import com.auruspay.comparator.XmlComparator;
import com.auruspay.decryptor.AurusDecryptor;
import com.auruspay.dto.ProcessRequest;
import com.auruspay.dto.UserInput;
import com.auruspay.service.JsonDataAddService;
import com.auruspay.service.TransactionLookupService;
import org.springframework.http.MediaType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Transaction APIs", description = "Process, Lookup and Compare transactions")
@RestController
@RequestMapping("/api")
public class ProcessController {

    private static final Logger logger =
            LoggerFactory.getLogger(ProcessController.class);

    private final UiController uiController;
    private final JsonDataAddService jsonDataAddService;
    private final TransactionLookupService lookupService;

    @Autowired
    private AurusDecryptor aurusDecryptor;

    @Autowired
    private XmlComparator xmlComparator;

    @Autowired
    private CctComparator cctComparator;

    public ProcessController(JsonDataAddService jsonDataAddService,
                             TransactionLookupService lookupService,
                             UiController uiController) {
        this.jsonDataAddService = jsonDataAddService;
        this.lookupService = lookupService;
        this.uiController = uiController;
    }

    @PostMapping( value = "/process" )
    public ResponseEntity<String> processAndSave(
            @RequestBody ProcessRequest request) {

        try {

            logger.info("Received transaction save request");

            String txnId = jsonDataAddService.saveData(request);

            return ResponseEntity.ok(txnId);

        } catch (Exception e) {

            logger.error("Error while saving transaction", e);

            return ResponseEntity.internalServerError()
                    .body("Failed to save transaction");
        }
    }

    // ================= DECRYPT =================
    @PostMapping("/decrypt")
    public ResponseEntity<String> decrypt(@RequestBody String encryptedData) {

        logger.info("Decrypt request received");

        if (encryptedData == null || encryptedData.trim().isEmpty()) {
            logger.warn("Empty encrypted data received");
            return ResponseEntity.badRequest().body("Input data is required");
        }

        try {
            String decryptedData = aurusDecryptor.decryptor(encryptedData);

            logger.info("Decryption completed successfully");
            return ResponseEntity.ok(decryptedData);

        } catch (Exception e) {

            logger.error("Decryption failed", e);

            return ResponseEntity.internalServerError()
                    .body("Decryption failed: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public String openForms() {

        logger.info("Test API called");
        return "Sucessfully tested...!";
    }

    // ================= MAIN COMPARE API =================
    @PostMapping(
    	    value = "/compare",
    	    consumes = {
    	        MediaType.APPLICATION_JSON_VALUE,
    	        MediaType.TEXT_PLAIN_VALUE
    	    },
    	    produces = {
    	        MediaType.APPLICATION_JSON_VALUE,
    	        MediaType.TEXT_PLAIN_VALUE
    	    }
    	)
    public ResponseEntity<?> compare(@RequestBody UserInput request) throws Exception {

        logger.info("Compare request received"+request);

        ProcessRequest declinedRequest = new ProcessRequest();

        String cctRequest = request.getCctRequest() != null
                ? aurusDecryptor.decryptor(request.getCctRequest())
                : null;

        String procRequest = request.getProcessorRequest() != null
                ? aurusDecryptor.decryptor(request.getProcessorRequest())
                : null;

        declinedRequest.setCctRequest(cctRequest);
        declinedRequest.setProcessorRequest(procRequest);

        logger.debug("CCT Request : {}", cctRequest);
        logger.debug("Processor Request : {}", procRequest);

        // ================= FETCH APPROVED DATA =================
        ProcessRequest approvedRequest =
                lookupService.lookupTransaction(declinedRequest);

        if (approvedRequest == null) {

            logger.warn("Approved transaction not found");
            return ResponseEntity.status(404)
                    .body("❌ Approved transaction not found");
        }

        logger.info("Approved transaction found :"+approvedRequest.getProcessorRequest());

        // ================= XML COMPARISON =================
        String approvedXml = approvedRequest.getProcessorRequest();
        String declinedXml = declinedRequest.getProcessorRequest();

        List<Map<String, String>> xmlComparedData =
                xmlComparator.getXmlComparator(approvedXml, declinedXml);

        logger.info("XML comparison completed. Differences count: {}",
                xmlComparedData.size());

        // ================= JSON/CCT COMPARISON =================
        String approvedJson = approvedRequest.getCctRequest();
        String declinedJson = declinedRequest.getCctRequest();

        List<Map<String, String>> cctComparedData =
                cctComparator.compare(declinedJson, approvedJson);

        logger.info("CCT comparison completed. Differences count: {}",
                cctComparedData.size());

        // ================= FINAL RESPONSE =================
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("xmlComparison", xmlComparedData);
        response.put("cctComparison", cctComparedData);

        logger.debug("XML Comparison Result: {}", xmlComparedData);
        logger.debug("CCT Comparison Result: {}", cctComparedData);

        logger.info("Compare API completed successfully");

        return ResponseEntity.ok(response);
    }
}