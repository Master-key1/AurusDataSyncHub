package com.auruspay.controller;

import com.auruspay.comparator.XmlComparator;
import com.auruspay.comparator.model.ComparisionXmlResult;
import com.auruspay.decryptor.AurusDecryptor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Tag(name = "Transaction APIs", description = "Process, Lookup and Compare transactions")
public class ValidatorController {
    private static final Logger logger = LoggerFactory.getLogger(ValidatorController.class);

    @Autowired
    private AurusDecryptor aurusDecryptor;

    @Autowired
    private XmlComparator xmlComparator;

    @GetMapping("/")
    public String loadPage() {
        return "xml-validator";
    }

    @PostMapping("/xml/compare1")
    public String compare(@RequestParam String approvedXml, 
                          @RequestParam String declinedXml, 
                          Model model) {
        try {
            String approved = aurusDecryptor.decryptor(approvedXml);
            String declined = aurusDecryptor.decryptor(declinedXml);

            ComparisionXmlResult result = xmlComparator.getXmlComparator(approved, declined);

            // Populate the specific lists for the UI
            model.addAttribute("matchList", result.getXmlMatchIssue());
            model.addAttribute("mismatchList", result.getXmlMissMatchIssue());
            model.addAttribute("skippedList", result.getXmlSkippedIssue());
            model.addAttribute("validationList", result.getXmlValidationIssue());

            model.addAttribute("approvedXml", approved);
            model.addAttribute("declinedXml", declined);

        } catch (Exception e) {
            logger.error("Compare failed", e);
            model.addAttribute("error", "Unable to decrypt/compare XML payload. Ensure keys are valid.");
        }
        return "xml-validator";
    }
    
    @PostMapping("/xml/compare")
    public String comparex(@RequestParam String approvedXml, @RequestParam String declinedXml, Model model) {
        try {
        	  String approved = aurusDecryptor.decryptor(approvedXml);
              String declined = aurusDecryptor.decryptor(declinedXml);
            // ... decryption logic ...
            ComparisionXmlResult result = xmlComparator.getXmlComparator(approved, declined);

            // Pass each list to the model
            model.addAttribute("matchList", result.getXmlMatchIssue());
            model.addAttribute("mismatchList", result.getXmlMissMatchIssue());
            model.addAttribute("skippedList", result.getXmlSkippedIssue());
            model.addAttribute("validationList", result.getXmlValidationIssue());
            
            model.addAttribute("approvedXml", approved);
            model.addAttribute("declinedXml", declined);
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
        }
        return "xml-validator";
    }
}