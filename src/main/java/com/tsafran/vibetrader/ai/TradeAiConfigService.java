package com.tsafran.vibetrader.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class TradeAiConfigService {
    private static final String CONFIG_DIR = "trade-ai";
    private static final String DEFAULT_CONFIG_NAME = "default";

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    public TradeAiConfigService(ObjectMapper objectMapper, ResourceLoader resourceLoader) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    public String resolveConfigName(String configName) {
        if (configName == null || configName.isBlank()) {
            return DEFAULT_CONFIG_NAME;
        }
        return configName.trim();
    }

    public TradeAiSettings loadConfig(String configName) {
        String resolvedName = resolveConfigName(configName);
        Resource resource = resourceLoader.getResource(
                "classpath:" + CONFIG_DIR + "/" + resolvedName + ".json"
        );
        if (!resource.exists()) {
            throw new IllegalArgumentException("AI config not found: " + resolvedName);
        }
        try (InputStream input = resource.getInputStream()) {
            return objectMapper.readValue(input, TradeAiSettings.class);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load AI config: " + resolvedName, ex);
        }
    }
}
