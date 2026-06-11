package com.auruspay.service;

import com.auruspay.dto.ProcessRequest;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DataParsingService {

    // ✅ INPUT IS ProcessRequest NOW
    public ProcessRequest process(ProcessRequest request) {

        ProcessRequest response = new ProcessRequest();

        response.setCctRequest(extractJson(request.getCctRequest(), "cct_Request"));

        response.setProcessorRequest(
                extractGMF(request.getProcessorRequest(), "processor_request"));

        response.setProcessorResponse(
                extractGMF(request.getProcessorResponse(), "processor_response"));

        response.setCctResponse(
                extractJson(request.getCctResponse(), "cct_response"));

        return response;
    }

    // ================= JSON =================
    private String extractJson(String data, String key) {

        if (data == null || data.isEmpty()) {
            return "INVALID INPUT";
        }

        Pattern pattern = Pattern.compile(key + "\\s*:\\s*(\\{.*?\\})", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(data);

        return matcher.find() ? matcher.group(1).trim() : "NOT FOUND";
    }

    // ================= GMF =================
    private String extractGMF(String data, String key) {

        if (data == null || data.isEmpty()) {
            return "INVALID INPUT";
        }

        Pattern pattern = Pattern.compile(key + "\\s*:\\s*([\\s\\S]*?<GMF[\\s\\S]*?</GMF>)");
        Matcher matcher = pattern.matcher(data);

        if (matcher.find()) {
            String block = matcher.group(1);

            Pattern gmfPattern = Pattern.compile("<GMF[\\s\\S]*?</GMF>");
            Matcher gmfMatcher = gmfPattern.matcher(block);

            if (gmfMatcher.find()) {
                return gmfMatcher.group(0).trim();
            }
        }

        return "NOT FOUND";
    }
}