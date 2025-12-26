package com.tsafran.vibetrader.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsafran.vibetrader.exchange.ExchangeOrderSide;
import com.tsafran.vibetrader.position.ProposedPosition;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

@Service
public class OpenAiTradeService implements AiTradeService {
    private static final String SCHEMA_NAME = "trade_proposal";

    private final ChatClient chatClient;
    private final BeanOutputConverter<AiTradeResponse> outputConverter;
    private final OpenAiChatOptions chatOptions;

    public OpenAiTradeService(ChatClient.Builder builder, ObjectMapper objectMapper) {
        this.chatClient = builder.build();
        this.outputConverter = new BeanOutputConverter<>(AiTradeResponse.class, objectMapper);
        this.chatOptions = OpenAiChatOptions.builder()
                .model("gpt-5.2")
                .responseFormat(ResponseFormat.builder()
                        .type(ResponseFormat.Type.JSON_SCHEMA)
                        .jsonSchema(ResponseFormat.JsonSchema.builder()
                                .name(SCHEMA_NAME)
                                .schema(outputConverter.getJsonSchemaMap())
                                .strict(true)
                                .build())
                        .build())
                .build();
    }

    @Override
    public AiTradeProposal proposeTrade(String symbol, String systemMessage, String userMessage) {
        Objects.requireNonNull(systemMessage, "systemMessage");
        Objects.requireNonNull(userMessage, "userMessage");

        AiTradeResponse response = chatClient.prompt()
                .options(chatOptions)
                .system(systemMessage)
                .user(userMessage)
                .call()
                .entity(outputConverter);

        if (response == null) {
            throw new IllegalStateException("AI response was empty");
        }

        return new AiTradeProposal(response.reasoning(), toProposedPosition(symbol, response.trade()));
    }

    private ProposedPosition toProposedPosition(String symbol, AiTradeResponse.Trade trade) {
        String sideValue = requireText(trade.side(), "trade.side");
        if (sideValue.trim().equalsIgnoreCase("NONE")) {
            return null;
        }

        ExchangeOrderSide side = parseSide(sideValue);
        BigDecimal entryPrice = parseDecimal(trade.entryPrice(), "trade.entryPrice");
        BigDecimal takeProfitPrice = parseDecimal(trade.takeProfitPrice(), "trade.takeProfitPrice");
        BigDecimal stopLossPrice = parseDecimal(trade.stopLossPrice(), "trade.stopLossPrice");

        return new ProposedPosition(symbol, side, entryPrice, takeProfitPrice, stopLossPrice);
    }

    private ExchangeOrderSide parseSide(String side) {
        try {
            return ExchangeOrderSide.valueOf(side.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("trade.side must be LONG or SHORT (or NONE for no trade)", e);
        }
    }

    private BigDecimal parseDecimal(String value, String fieldName) {
        String text = requireText(value, fieldName);
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(fieldName + " must be a decimal value", e);
        }
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(fieldName + " must be provided");
        }
        return value;
    }

    private record AiTradeResponse(String reasoning, Trade trade) {
        private record Trade(
                String side,
                String entryPrice,
                String takeProfitPrice,
                String stopLossPrice
        ) {
        }
    }
}
