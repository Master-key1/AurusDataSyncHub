package com.auruspay.comparator.controller;

import com.auruspay.comparator.XmlComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/xml")
public class XmlValidatorController {
    @Autowired private XmlComparator xmlComparator;

    @GetMapping
    public String loadPage() { return "xml-validator"; }

    @PostMapping("/compare")
    public String compare(@RequestParam String approvedXml, @RequestParam String declinedXml, Model model) {
        model.addAttribute("results", xmlComparator.getXmlComparator(approvedXml, declinedXml));
        model.addAttribute("approvedXml", approvedXml);
        model.addAttribute("declinedXml", declinedXml);
        return "xml-validator";
    }
}