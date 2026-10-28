
package com.auruspay.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.jpos.iso.ISOUtil;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.auruspay.config.TransactionProperties;
import com.auruspay.dto.TransactionRequestDto;
import com.auruspay.dto.TransactionResponseDto;

@Service
public class TransactionService {

    private final TransactionProperties properties;

    public TransactionService(
            TransactionProperties properties) {

        this.properties = properties;
    }

    public TransactionResponseDto submit(
            TransactionRequestDto dto) {

        TransactionResponseDto response =
                new TransactionResponseDto();

        HttpURLConnection connection = null;

        try {

            String endpoint =
                    resolveEndpoint(dto);

            String request =
                    "{"
                            + "\"formFactorId\":\"71310430\","
                            + "\"txnDateTime\":\"02032015120019\","
                            + "\"encryptionFlag\":\"00\","
                            + "\"payload\":"
                            + dto.getRequest()
                            + "}";

            JSONObject jsonObject =
                    new JSONObject(request);

            String originalPayload =
                    jsonObject.get("payload")
                            .toString();

            String encodedPayload =
                    "STX"
                            + ISOUtil.byte2hex(
                                    originalPayload.getBytes())
                            + "ETX";

            jsonObject.put(
                    "payload",
                    encodedPayload);

            URL url =
                    new URL(endpoint);

            connection =
                    (HttpURLConnection)
                            url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            connection.setRequestProperty(
                    "Content-Type",
                    "application/json");

            try (OutputStream out =
                         connection.getOutputStream()) {

                out.write(
                        jsonObject.toString()
                                .getBytes());

                out.flush();
            }

            StringBuilder result =
                    new StringBuilder();

            try (BufferedReader in =
                         new BufferedReader(
                                 new InputStreamReader(
                                         connection.getInputStream()))) {

                String line;

                while ((line = in.readLine()) != null) {

                    result.append(line);
                }
            }

            String rawResponse =
                    result.toString();

            String decodedResponse =
                    decodeResponsePayload(
                            rawResponse);

            response.setMessage("Transaction Success");

            response.setEndpoint(
                    endpoint);

            response.setRequestPayload(
                    originalPayload);


            response.setDecodedResponse(
                    decodedResponse);

        } catch (Exception e) {

           
            response.setMessage(
                    e.getMessage());

        } finally {

            if (connection != null) {

                connection.disconnect();
            }
        }

        return response;
    }

    private String resolveEndpoint(
            TransactionRequestDto dto) {

        if (dto.getCustomUrl() != null
                && !dto.getCustomUrl().isBlank()) {

            return dto.getCustomUrl()
                    .trim();
        }

        String endpoint =
                properties.getUrls()
                        .get(dto.getEnvironment());

        if (endpoint == null) {

            throw new RuntimeException(
                    "Invalid Environment : "
                            + dto.getEnvironment());
        }

        return endpoint;
    }

    private String decodeResponsePayload(
            String response) {

        try {

            int stx =
                    response.indexOf("STX");

            int etx =
                    response.indexOf("ETX");

            if (stx >= 0
                    && etx > stx) {

                String hexPayload =
                        response.substring(
                                stx + 3,
                                etx);

                String decoded =
                        convertHexToString(
                                hexPayload);

                try {

                    JSONObject json =
                            new JSONObject(
                                    decoded);

                    return json.toString(4);

                } catch (Exception ex) {

                    return decoded;
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return response;
    }

    private String convertHexToString(
            String hex) {

        StringBuilder sb =
                new StringBuilder();

        try {

            for (int i = 0;
                 i < hex.length() - 1;
                 i += 2) {

                String output =
                        hex.substring(
                                i,
                                i + 2);

                int decimal =
                        Integer.parseInt(
                                output,
                                16);

                sb.append(
                        (char) decimal);
            }

        } catch (Exception e) {

            return hex;
        }

        return sb.toString();
    }
}

