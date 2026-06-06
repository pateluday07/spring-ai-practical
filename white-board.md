# Spring AI Multi-Provider Demo

<h2 style="color:#2563eb;">1. Add Both Provider Dependencies in pom.xml</h2>

- `spring-ai-starter-model-openai`
- `spring-ai-starter-model-google-genai`
- keep `spring-ai-bom` in `dependencyManagement`

<h2 style="color:#0891b2;">2. Configure Providers in application.yaml</h2>

- `spring.ai.openai.*` → api-key, model, temperature, max tokens
- `spring.ai.google.genai.*` → api-key, model, temperature, max tokens
- `app.ai.default-provider: openai` → fallback when request has no provider

<h2 style="color:#7c3aed;">3. Extend ChatRequest DTO</h2>

- add `provider` field
- keep `model`, `temperature`, `maxCompletionTokens` for per-request overrides
- add `hasModelOverrides()` helper

<h2 style="color:#16a34a;">4. Create ChatProvider Interface</h2>

- `id()` → `"openai"` / `"gemini"`
- `chatModel()` → Spring AI `ChatModel`
- `toOptions(ChatRequest)` → provider-specific `ChatOptions`
- default `chat()` and `stream()` methods on the interface

<h2 style="color:#16a34a;">5. Implement Provider Beans</h2>

- `OpenAiChatProvider` → inject `OpenAiChatModel`, build `OpenAiChatOptions`
- `GeminiChatProvider` → inject `GoogleGenAiChatModel`, build `GoogleGenAiChatOptions`
- annotate both with `@Component`

<h2 style="color:#ca8a04;">6. Build ChatProviderRegistry</h2>

- constructor takes `List<ChatProvider>` from Spring
- collect into `Map<id, ChatProvider>`
- `resolve(request, defaultProviderId)` → pick request provider or default

<h2 style="color:#9333ea;">7. Extract ChatPromptFactory</h2>

- read default system prompt from `spring.ai.system`
- build `SystemMessage` + `UserMessage`
- attach provider-specific options via `provider.toOptions(request)`

<h2 style="color:#9333ea;">8. Refactor AiChatServiceImpl</h2>

- inject `ChatProviderRegistry` + `ChatPromptFactory`
- `resolve` → `create prompt` → `provider.chat()` / `provider.stream()`
- remove direct `ChatModel` dependency from service

<h2 style="color:#ea580c;">9. Update the GUI</h2>

- provider dropdown → `openai` / `gemini`
- default model per provider in `app.js`
- send `provider` in JSON request body

<h2 style="color:#dc2626;">10. Demo Multi-Provider Switching</h2>

- same prompt, switch OpenAI → Gemini
- show model defaults change per provider
- show streaming works for both
