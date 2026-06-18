package com.auruspay.comparator.config;

import com.auruspay.comparator.model.IsoFieldDefinition;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class IsoFieldDefinitionLoader {

    private final Map<String, IsoFieldDefinition> fieldMap =
            new HashMap<>();

    @PostConstruct
    public void loadDefinitions() {

        try {

        	InputStream inputStream =
        	        Thread.currentThread()
        	              .getContextClassLoader()
        	              .getResourceAsStream("static/iso-fields-definition.xml");

            if (inputStream == null) {
                throw new RuntimeException(
                        "iso-fields.xml not found in resources");
            }

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            DocumentBuilder builder =
                    factory.newDocumentBuilder();

            Document document =
                    builder.parse(inputStream);

            NodeList nodeList =
                    document.getElementsByTagName("isofield");

            for (int i = 0; i < nodeList.getLength(); i++) {

                Element element =
                        (Element) nodeList.item(i);

                IsoFieldDefinition field =
                        new IsoFieldDefinition();

                field.setId(
                        element.getAttribute("id"));

                field.setName(
                        element.getAttribute("name"));

                field.setClassType(
                        element.getAttribute("classType"));

                field.setFailedMsg(
                        element.getAttribute("failedMsg"));

                field.setMinLength(
                        Integer.parseInt(
                                element.getAttribute("minlength")));

                field.setMaxLength(
                        Integer.parseInt(
                                element.getAttribute("maxlength")));

                fieldMap.put(field.getId(), field);
            }

            System.out.println(
                    "ISO Field Definitions Loaded : "
                            + fieldMap.size());

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error loading iso-fields.xml", e);
        }
    }

    public IsoFieldDefinition getField(String fieldId) {
        return fieldMap.get(fieldId);
    }
}