package com.auruspay.controller;

import com.auruspay.dto.ProcessRequest;
import com.auruspay.service.JsonDataAddService;
import com.auruspay.service.TransactionLookupService;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProcessController {

    private final JsonDataAddService jsonDataAddService;
    private final  TransactionLookupService lookupService ;

    public ProcessController(JsonDataAddService jsonDataAddService, TransactionLookupService lookupService) {
        this.jsonDataAddService = jsonDataAddService;
        this.lookupService = lookupService;
    }

    // ✅ UI will call this API
    @PostMapping("/process")
    public String processAndSave(@RequestBody ProcessRequest request) throws Exception {
        return jsonDataAddService.saveData(request);
    }
    @PostMapping("/lookup")
    public ResponseEntity<?> lookup(@RequestBody String requestJson) throws Exception {

        ProcessRequest response = lookupService.lookupTransaction(requestJson);

        // check if data is not found (based on your service logic)
        if (response == null ||
            response.getCctRequest() == null ||
            response.getCctRequest().contains("NO_DATA_FOUND") ||
            response.getCctRequest().contains("DATA_FILE_NOT_FOUND")) {

            return ResponseEntity
                    .status(404)
                    .body("❌ Transaction not found");
        }

        return ResponseEntity.ok(response);
    }
    @PostMapping("/compared")
    public ResponseEntity<?> compare(@RequestBody ProcessRequest request) throws Exception {
    	System.out.println(request.getCctRequest());

        ProcessRequest response = lookupService.lookupTransaction(request.getCctRequest());

        // check if data is not found (based on your service logic)
        if (response == null ||
            response.getCctRequest() == null ||
            response.getCctRequest().contains("NO_DATA_FOUND") ||
            response.getCctRequest().contains("DATA_FILE_NOT_FOUND")) {

            return ResponseEntity
                    .status(404)
                    .body("❌ Transaction not found");
        }

        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/compare")
    public ProcessRequest compare(@RequestBody JsonNode requestJson) throws Exception {
        return lookupService.lookupTransaction(requestJson.toString());
    }
}