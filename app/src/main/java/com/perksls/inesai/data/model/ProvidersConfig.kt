package com.perksls.inesai.data.model

object ProvidersConfig {
    val SUGGESTIONS = listOf(
        ProviderSuggestion("OpenAI",           "https://api.openai.com/v1",                         listOf("gpt-4o", "gpt-4o-mini", "gpt-3.5-turbo"),             true),
        ProviderSuggestion("Groq",             "https://api.groq.com/openai/v1",                    listOf("llama-3.3-70b-versatile", "llama-3.1-8b-instant"),     true),
        ProviderSuggestion("Cerebras",         "https://api.cerebras.ai/v1",                        listOf("llama-3.1-8b", "llama-4-scout-17b-16e"),               true),
        ProviderSuggestion("Google AI Studio", "https://generativelanguage.googleapis.com/v1beta/openai", listOf("gemini-2.5-flash", "gemini-2.5-pro"),            true),
        ProviderSuggestion("GitHub Models",    "https://models.github.ai/inference",                listOf("gpt-4o", "llama-3.3-70b-instruct"),                    true),
        ProviderSuggestion("DeepSeek",         "https://api.deepseek.com/v1",                       listOf("deepseek-chat", "deepseek-reasoner"),                   true),
        ProviderSuggestion("OpenRouter",       "https://openrouter.ai/api/v1",                      listOf("openai/gpt-4o", "meta-llama/llama-3.3-70b-instruct"),  true),
        ProviderSuggestion("Mistral AI",       "https://api.mistral.ai/v1",                         listOf("mistral-large-latest", "mistral-small-latest"),         true),
        ProviderSuggestion("xAI (Grok)",       "https://api.x.ai/v1",                               listOf("grok-3", "grok-3-mini"),                                true),
        ProviderSuggestion("Together AI",      "https://api.together.xyz/v1",                       listOf("meta-llama/Llama-3.3-70B-Instruct-Turbo"),             true),
        ProviderSuggestion("Fireworks AI",     "https://api.fireworks.ai/inference/v1",             listOf("accounts/fireworks/models/llama-v3p3-70b-instruct"),    true),
        ProviderSuggestion("NVIDIA NIM",       "https://integrate.api.nvidia.com/v1",               listOf("meta/llama-3.3-70b-instruct"),                          true),
        ProviderSuggestion("Hugging Face",     "https://router.huggingface.co/v1",                  listOf("meta-llama/Llama-3.3-70B-Instruct"),                   true),
        ProviderSuggestion("Azure AI Foundry", "https://{resource}.openai.azure.com/openai/v1",     listOf("gpt-4o"),                                               true),
        ProviderSuggestion("Ollama (local)",   "http://localhost:11434/v1",                          listOf("llama3.2", "mistral", "phi4"),                          true),
    )
}

data class ProviderSuggestion(
    val name: String,
    val baseUrl: String,
    val models: List<String>,
    val isOpenAICompatible: Boolean = true
)
