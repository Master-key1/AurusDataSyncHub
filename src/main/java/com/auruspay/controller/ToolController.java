package com.auruspay.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.auruspay.decryptor.AurusDecryptor;
import com.auruspay.dto.TransactionRequestDto;
import com.auruspay.dto.TransactionResponseDto;
import com.auruspay.service.TransactionService;

@Controller
public class ToolController {

    private final AurusDecryptor aurusDecryptor;
    private final TransactionService transactionService;

    public ToolController(
            AurusDecryptor aurusDecryptor,
            TransactionService transactionService) {

        this.aurusDecryptor = aurusDecryptor;
        this.transactionService = transactionService;
    }

    /* ================= HOME ================= */

    @GetMapping("/xyz")
    public String home() {
        return "redirect:/decryptor";
    }

    /* ================= DECRYPTOR PAGE ================= */

    @GetMapping("/decryptor")
    public String decryptorPage(Model model) {

        model.addAttribute("activePage", "decryptor");

        if (!model.containsAttribute("inputText")) {
            model.addAttribute("inputText", "");
        }

        if (!model.containsAttribute("outputText")) {
            model.addAttribute("outputText", "");
        }

        return "decryptor";
    }

    /* ================= HANDLE DECRYPT ================= */

    @PostMapping("/decrypt")
    public String decrypt(
            @RequestParam("inputText") String inputText,
            RedirectAttributes redirectAttributes) {

        String decryptedOutput = "";

        try {

            decryptedOutput =
                    aurusDecryptor.decryptor(inputText);

        } catch (Exception e) {

            decryptedOutput =
                    "Error : " + e.getMessage();
        }

        redirectAttributes.addFlashAttribute(
                "inputText",
                inputText);

        redirectAttributes.addFlashAttribute(
                "outputText",
                decryptedOutput);

        return "redirect:/decryptor";
    }

    /* ================= SAFETY FIX ================= */

    @GetMapping("/decrypt")
    public String decryptGet() {
        return "redirect:/decryptor";
    }

    /* ================= COMPARATOR PAGE ================= */

    @GetMapping("/comparator")
    public String comparator(Model model) {

        model.addAttribute(
                "activePage",
                "comparator");

        return "comparator";
    }

    /* ================= COMPARATOR HANDLE ================= */

    /*
    @PostMapping("/compare")
    public String compare(
            @RequestParam("a") String a,
            @RequestParam("b") String b,
            @RequestParam("mode") String mode,
            Model model) {

        model.addAttribute(
                "activePage",
                "comparator");

        String formattedA =
                formatInput(a, mode);

        String formattedB =
                formatInput(b, mode);

        model.addAttribute(
                "a",
                formattedA);

        model.addAttribute(
                "b",
                formattedB);

        return "diff";
    }
*/
    /* ================= BEAUTIFIER ================= */

    @GetMapping("/beautifier")
    public String beautifier(Model model) {

        model.addAttribute(
                "activePage",
                "beautifier");

        return "beautifier";
    }

    /* ================= TRANSACTION PAGE ================= */

    @GetMapping("/transaction")
    public String transaction(Model model) {

        model.addAttribute(
                "activePage",
                "transaction");

        return "transaction";
    }

    /* ================= TRANSACTION API ================= */

    @ResponseBody
    @PostMapping("/api/transaction/submit")
    public ResponseEntity<TransactionResponseDto>
    submitTransaction(
            @RequestBody
            TransactionRequestDto request) {

        TransactionResponseDto response =
                transactionService.submit(request);

        return ResponseEntity.ok(response);
    }

    /* ================= FORMAT HELPER ================= */

    private String formatInput(
            String text,
            String mode) {

        try {

            if ("json".equalsIgnoreCase(mode)) {

                com.fasterxml.jackson.databind.ObjectMapper mapper =
                        new com.fasterxml.jackson.databind.ObjectMapper();

                Object obj =
                        mapper.readValue(
                                text,
                                Object.class);

                return mapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(obj);
            }

            if ("xml".equalsIgnoreCase(mode)) {

                return text.replaceAll(
                        ">\\s*<",
                        ">\n<");
            }

        } catch (Exception e) {

            return text;
        }

        return text;
    }
}
