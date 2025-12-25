package com.tsafran.vibetrader.exchange.bybit;

import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.restApi.BybitApiAccountRestClient;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import com.bybit.api.client.restApi.BybitApiTradeRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BybitConfig {
    @Value("${bybit.api-key:}")
    private String apiKey;

    @Value("${bybit.api-secret:}")
    private String apiSecret;

    @Bean
    public BybitApiMarketRestClient bybitApiMarketRestClient() {
        return BybitApiClientFactory.newInstance().newMarketDataRestClient();
    }

    @Bean
    public BybitApiTradeRestClient bybitApiTradeRestClient() {
        if (apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) {
            return BybitApiClientFactory.newInstance().newTradeRestClient();
        }

        return BybitApiClientFactory.newInstance(apiKey, apiSecret, BybitApiConfig.DEMO_TRADING_DOMAIN).newTradeRestClient();
    }

    @Bean
    public BybitApiAccountRestClient bybitApiAccountRestClient() {
        if (apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) {
            return BybitApiClientFactory.newInstance().newAccountRestClient();
        }

        return BybitApiClientFactory.newInstance(apiKey, apiSecret, BybitApiConfig.DEMO_TRADING_DOMAIN).newAccountRestClient();
    }
}
