package io.github.vinipx.quantaf.ai;

/**
 * Pluggable interface for Large Language Model providers.
 * Implementations wrap specific LLM backends (OpenAI, Ollama, Anthropic, etc.).
 */
public interface LlmProvider {

    /**
     * Sends a completion request to the LLM.
     *
     * @param systemPrompt the system prompt (instructions for the model)
     * @param userMessage  the user message (the actual request)
     * @return the model's response text
     */
    String complete(String systemPrompt, String userMessage);

    /**
     * Returns the provider name (e.g., "openai", "ollama", "anthropic").
     */
    String getProviderName();

    /**
     * Returns the model identifier being used.
     */
    String getModelName();

    /**
     * Checks if the provider is available and reachable.
     */
    boolean isAvailable();
}
