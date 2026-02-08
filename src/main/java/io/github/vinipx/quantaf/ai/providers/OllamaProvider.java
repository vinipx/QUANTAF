package io.github.vinipx.quantaf.ai.providers;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import io.github.vinipx.quantaf.ai.LlmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ollama (local LLM) provider implementation using LangChain4j.
 * No external API dependency -- runs against a local Ollama server.
 */
public class OllamaProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(OllamaProvider.class);

    private final ChatLanguageModel model;
    private final String modelName;

    public OllamaProvider(String baseUrl, String modelName) {
        this.modelName = modelName;
        this.model = OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.1)
                .build();
        log.info("Ollama provider initialized [baseUrl={}, model={}]", baseUrl, modelName);
    }

    @Override
    public String complete(String systemPrompt, String userMessage) {
        try {
            String fullPrompt = systemPrompt + "\n\nUser: " + userMessage;
            String response = model.generate(fullPrompt);
            log.debug("Ollama response ({} chars)", response.length());
            return response;
        } catch (Exception e) {
            log.error("Ollama completion failed", e);
            throw new RuntimeException("Ollama completion failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "ollama";
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
