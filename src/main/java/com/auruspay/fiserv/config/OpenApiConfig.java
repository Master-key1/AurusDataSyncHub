package com.auruspay.config;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Aurus API",
                version = "1.0",
                description = "Transaction Comparison APIs"
        )
)
public class OpenApiConfig {
}