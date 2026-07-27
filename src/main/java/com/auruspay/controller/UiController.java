package com.auruspay.controller;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.auruspay.comparator.CctComparator;
import com.auruspay.comparator.XmlComparator;
import com.auruspay.decryptor.AurusDecryptor;
import com.auruspay.dto.ProcessRequest;
import com.auruspay.dto.UserInput;
import com.auruspay.service.JsonDataAddService;
import com.auruspay.service.TransactionLookupService;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UiController {
	 private static final Logger logger =
	            LoggerFactory.getLogger(UiController.class);

	 @Autowired
	    private AurusDecryptor aurusDecryptor;
	  @Autowired
	    private XmlComparator xmlComparator;

	    @Autowired
	    private CctComparator cctComparator;
	    
	    @Autowired
	    private  TransactionLookupService lookupService;
	    
    private final JsonDataAddService jsonDataAddService;

    public UiController(JsonDataAddService jsonDataAddService) {
        this.jsonDataAddService = jsonDataAddService;
    }
    @GetMapping("/ui")
    public String openForm(Model model) {

        if (!model.containsAttribute("processRequest")) {
            model.addAttribute("processRequest", new ProcessRequest());
        }

        return "process-form";
    }

    @PostMapping("/ui")
    public String processForm(
            @ModelAttribute ProcessRequest request,
            Model model) {
        try {

            // Validation
            if (request.getCctRequest() == null ||
                    request.getCctRequest().trim().isEmpty()) {

                model.addAttribute("errorMessage",
                        "CCT Request is required.");

                model.addAttribute("processRequest", request);

                return "process-form";
            }

            if (request.getCctResponse() == null ||
                    request.getCctResponse().trim().isEmpty()) {

                model.addAttribute("errorMessage",
                        "CCT Response is required.");

                model.addAttribute("processRequest", request);

                return "process-form";
            }

            String txnId = jsonDataAddService.saveData(request);

            model.addAttribute("message",
                    "Transaction saved successfully. Txn Id : " + txnId);

            // KEEP VALUES AFTER SAVE
            model.addAttribute("processRequest", request);

        } catch (MismatchedInputException ex) {

            model.addAttribute("errorMessage",
                    "Invalid JSON format. Please verify CCT Request/CCT Response.");

            model.addAttribute("processRequest", request);

        } catch (Exception ex) {

            model.addAttribute("errorMessage",
                    ex.getMessage());

            model.addAttribute("processRequest", request);
        }

        return "process-form";
    }
    @GetMapping("/diffcompare")
    public String comparator(Model model) {

        model.addAttribute("cctRequest", "");
        model.addAttribute("processorRequest", "");

        return "diffcompare";
    }
    

   
    @PostMapping("/diffcompare/{processorid}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> diffCompare(
            @RequestParam String cctRequest,
            @RequestParam String processorRequest,
            @PathVariable String processorid) throws Exception {

        logger.info("Compare cctRequest received : "+cctRequest);
        logger.info("Compare processorRequest received :"+processorRequest);

        ProcessRequest declinedRequest = new ProcessRequest();

        String cct_Request = cctRequest.trim() != null
                ? aurusDecryptor.decryptor(cctRequest.trim())
                : null;

        String procRequest = processorRequest.trim() != null
                ? aurusDecryptor.decryptor(processorRequest.trim())
                : null;

        declinedRequest.setCctRequest(cct_Request);
        declinedRequest.setProcessorRequest(procRequest);

        logger.info("CCT Request : {}", cct_Request);
        logger.info("Processor Request : {}", procRequest);

        // ================= FETCH APPROVED DATA =================
   
                lookupService.lookupTransaction(declinedRequest ,processorid);

        if ("" == null) {

            logger.warn("Approved transaction not found");
            return null;
        }

        logger.info("Approved transaction found :");//+approvedRequest.getProcessorRequest());

        // ================= XML COMPARISON =================
        String approvedXml = null ;//approvedRequest.getProcessorRequest();
        String declinedXml = declinedRequest.getProcessorRequest();

   // xmlComparedData = xmlComparator.getXmlComparator(approvedXml, declinedXml);

        logger.info("XML comparison completed. Differences count: {}");
              //  xmlComparedData.size());

        // ================= JSON/CCT COMPARISON =================
        String approvedJson = null;// approvedRequest.getCctRequest();
        String declinedJson = declinedRequest.getCctRequest();

        List<Map<String, String>> cctComparedData =
                cctComparator.compare(declinedJson, approvedJson);

        logger.info("CCT comparison completed. Differences count: {}",
                cctComparedData.size());

        // ================= FINAL RESPONSE =================
        Map<String, Object> response = new LinkedHashMap<>();

       // response.put("xmlComparison", xmlComparedData);
        response.put("cctComparison", cctComparedData);

      //  logger.debug("XML Comparison Result: {}", xmlComparedData);
        logger.debug("CCT Comparison Result: {}", cctComparedData);

        logger.info("Compare API completed successfully");

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/json")
    public String home() {
        return "smartcomparator";
    }

    @GetMapping("/json/compare")
    public String smartComparator() {
        return "smartcomparator";
    }

    }
