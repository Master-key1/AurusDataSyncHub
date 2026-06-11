package com.auruspay.controller;

import com.auruspay.dto.ProcessRequest;
import com.auruspay.service.JsonDataAddService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UiController {

    private final JsonDataAddService jsonDataAddService;

    public UiController(JsonDataAddService jsonDataAddService) {
        this.jsonDataAddService = jsonDataAddService;
    }

    // ✅ Load UI page
    @GetMapping({"/save","/"})
    public String openForm(Model model) {
        model.addAttribute("processRequest", new ProcessRequest());
        return "process-form";
    }

    // ✅ Submit form
    @PostMapping("/ui/process")
    public String processForm(@ModelAttribute ProcessRequest request, Model model) throws Exception {

        String result = jsonDataAddService.saveData(request);

        model.addAttribute("message", result);
        model.addAttribute("processRequest", new ProcessRequest());

        return "process-form";
    }
}