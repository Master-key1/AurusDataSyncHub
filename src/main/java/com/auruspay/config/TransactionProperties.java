package com.auruspay.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "transaction")
public class TransactionProperties {

    private Map<String,String> urls;

    public Map<String, String> getUrls() {
        return urls;
    }

    public void setUrls(Map<String, String> urls) {
        this.urls = urls;
    }
}