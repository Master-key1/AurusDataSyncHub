package com.auruspay.service;

import com.auruspay.dto.ProcessRequest;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DataParsingService {

    public ProcessRequest process(ProcessRequest request) {

        ProcessRequest response = new ProcessRequest();

        response.setCctRequest(
                extractJson(request.getCctRequest(), "cct_Request"));

        response.setProcessorRequest(
                extractGmf(request.getProcessorRequest(), "processor_request"));

        response.setProcessorResponse(
                extractGmf(request.getProcessorResponse(), "processor_response"));

        response.setCctResponse(
                extractJson(request.getCctResponse(), "cct_response"));

        return response;
    }

    // =====================================
    // JSON EXTRACTION
    // =====================================
    private String extractJson(String data, String key) {

        if (data == null || data.isBlank()) {
            return null;
        }

        int start = data.indexOf('{');

        if (start == -1) {
            return null;
        }

        int braces = 0;

        for (int i = start; i < data.length(); i++) {

            char ch = data.charAt(i);

            if (ch == '{') {
                braces++;
            } else if (ch == '}') {
                braces--;

                if (braces == 0) {
                    return data.substring(start, i + 1).trim();
                }
            }
        }

        return null;
    }

    // =====================================
    // GMF XML EXTRACTION
    // =====================================
    private String extractGmf(String data, String key) {

        if (data == null || data.isBlank()) {
            return null;
        }

        Pattern pattern =
                Pattern.compile("<GMF[\\s\\S]*?</GMF>");

        Matcher matcher = pattern.matcher(data);

        if (matcher.find()) {
            return matcher.group().trim();
        }

        return null;
    }
}