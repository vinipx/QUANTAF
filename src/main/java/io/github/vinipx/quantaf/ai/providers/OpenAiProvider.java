package io.github.vinipx.quantaf.ai.providers;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.github.vinipx.quantaf.ai.LlmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenAI LLM provider implementation using LangChain4j.
 */
public class OpenAiProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiProvider.class);

    private final ChatLanguageModel model;
    private final String modelName;

    public OpenAiProvider(String apiKey, String modelName) {
        this.modelName = modelName;
        this.model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.1)
                .build();
        log.info("OpenAI provider initialized [model={}]", modelName);
    }

    @Override
    public String complete(String systemPrompt, String userMessage) {
        try {
            String fullPrompt = systemPrompt + "\n\nUser: " + userMessage;
            String response = model.generate(fullPrompt);
            log.debug("OpenAI response ({} chars)", response.length());
            return response;
        } catch (Exception e) {
            log.error("OpenAI completion failed", e);
            throw new RuntimeException("OpenAI completion failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public boolean isAvailable() {
        try {
            model.generate("ping");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
