package com.auruspay.vantiv.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.auruspay.vantiv.dto.ComparisonReport;
import com.auruspay.vantiv.service.MessageComparisonService;

@RestController
public class Iso8583CompareController {

    private final MessageComparisonService comparisonService;

    public Iso8583CompareController(MessageComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    public record CompareRequest(String approvedHex, String declinedHex) {
    }

    @PostMapping("/api/iso8583/compare")
    public ComparisonReport compare(
            @RequestBody CompareRequest request) {

    	return comparisonService.compare(
    	        request.approvedHex(),
    	        request.declinedHex());
    }
}
