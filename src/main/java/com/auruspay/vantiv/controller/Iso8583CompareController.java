package com.worldpay.iso8583.controller;

import com.worldpay.iso8583.dto.ComparisonReport;
import com.worldpay.iso8583.dto.FieldComparisonResult;
import com.worldpay.iso8583.service.MessageComparisonService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
