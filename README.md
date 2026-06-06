# Spring AI Practical

Spring Boot app that sends chat requests to multiple AI providers through Spring AI. It supports OpenAI and Google Gemini, with both a normal JSON chat API and a streaming API that sends response chunks as Server-Sent Events.

**Flow:** Client -> `AiChatController` -> `AiChatServiceImpl` -> `ChatProviderRegistry` -> `ChatProvider` -> Spring AI `ChatModel` -> OpenAI or Gemini -> JSON or streamed Markdown response

**Stack:** Java 21, Spring Boot 3.5.14, Spring AI 1.1.5, OpenAI, Google Gemini, Maven

## Quick Start

1. Get API keys:
   - OpenAI: https://platform.openai.com/api-keys
   - Google Gemini: https://aistudio.google.com/apikey
2. Set env vars in the same terminal session:

```powershell
$env:OPENAI_API_KEY = "your-openai-api-key-here"
$env:GOOGLE_GENAI_API_KEY = "your-google-api-key-here"
# optional: OPENAI_CHAT_MODEL, OPENAI_TEMPERATURE, OPENAI_MAX_COMPLETION_TOKENS
# optional: GOOGLE_GENAI_CHAT_MODEL, GOOGLE_GENAI_TEMPERATURE, GOOGLE_GENAI_MAX_OUTPUT_TOKENS
# optional: APP_AI_DEFAULT_PROVIDER, SPRING_AI_SYSTEM
```

3. Run the app:

```powershell
mvn spring-boot:run
```

4. Open the UI:

```text
http://localhost:8080
```

Do not hardcode secrets in `application.yaml`.

## UI Demo

The app includes a small browser UI for demoing streaming responses:

```text
http://localhost:8080
```

The UI supports:

- prompt input
- optional system instructions
- provider selection (`openai` / `gemini`)
- model override
- max token override
- temperature override
- live streamed response
- Markdown rendering while chunks arrive
- raw chunk list for explaining streaming behavior

If **System instructions** is left blank, the backend uses `spring.ai.system` from `application.yaml`.

## APIs

### Normal Chat

```http
POST /api/ai/chat
Content-Type: application/json
```

Returns one complete JSON response after the model finishes.

```json
{
  "prompt": "How does Kubernetes handle self-healing?",
  "provider": "openai"
}
```

Response:

```json
{
  "response": "..."
}
```

### Streaming Chat

```http
POST /api/ai/chat/stream
Content-Type: application/json
Accept: text/event-stream
```

Returns chunks as Server-Sent Events using `MediaType.TEXT_EVENT_STREAM_VALUE`.

OpenAI example:

```json
{
  "prompt": "How does Kubernetes handle self-healing?",
  "provider": "openai",
  "model": "gpt-5.4-nano",
  "maxCompletionTokens": 1000,
  "temperature": 1
}
```

Gemini example:

```json
{
  "prompt": "How does Kubernetes handle self-healing?",
  "provider": "gemini",
  "model": "gemini-2.5-flash",
  "maxCompletionTokens": 1000,
  "temperature": 1
}
```

PowerShell curl test:

```powershell
curl.exe -N --location "http://localhost:8080/api/ai/chat/stream" `
  --header "Content-Type: application/json" `
  --header "Accept: text/event-stream" `
  --data-raw '{"prompt":"How does Kubernetes handle self-healing?","provider":"openai","model":"gpt-5.4-nano","maxCompletionTokens":1000,"temperature":1}'
```

The `-N` flag disables curl buffering so chunks appear as they arrive.

## Request Fields

All fields except `prompt` are optional.

| Field | Description |
|-------|-------------|
| `prompt` | User message |
| `system` | Optional system instruction override |
| `provider` | AI provider id: `openai` or `gemini` |
| `model` | Optional model override |
| `temperature` | Optional randomness setting |
| `maxCompletionTokens` | Optional max output token setting |

When `system` is omitted or blank, the backend uses `spring.ai.system`.

When `provider` is omitted or blank, the backend uses `app.ai.default-provider` (`openai` by default).

## Multi-Provider Architecture

The app uses a small provider abstraction so the controller and service stay provider-agnostic:

- `ChatProvider` — common interface for `id()`, `chatModel()`, `toOptions()`, `chat()`, and `stream()`
- `OpenAiChatProvider` — wraps `OpenAiChatModel` and builds `OpenAiChatOptions`
- `GeminiChatProvider` — wraps `GoogleGenAiChatModel` and builds `GoogleGenAiChatOptions`
- `ChatProviderRegistry` — Spring injects all `ChatProvider` beans and resolves the requested provider
- `ChatPromptFactory` — builds the prompt with system/user messages and provider-specific options

Adding another provider later means creating one new `@Component` that implements `ChatProvider`.

## Streaming Implementation

The controller exposes the stream as:

```java
@PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> stream(@RequestBody ChatRequest request) {
    return chatService.stream(request);
}
```

The service resolves the provider, builds the prompt, and delegates streaming:

```java
ChatProvider provider = providerRegistry.resolve(request, defaultProviderId);
Prompt prompt = promptFactory.create(request, provider);
return provider.stream(prompt);
```

The frontend calls the streaming endpoint with `fetch()`, reads `response.body.getReader()`, parses SSE `data:` chunks, and re-renders Markdown as text arrives.

## Configuration

See `src/main/resources/application.yaml`.

Key settings:

- `spring.ai.system`: default system prompt
- `spring.ai.openai.api-key`: from `OPENAI_API_KEY`
- `spring.ai.openai.chat.options.model`: default OpenAI model
- `spring.ai.openai.chat.options.temperature`: default OpenAI temperature
- `spring.ai.openai.chat.options.max-completion-tokens`: default OpenAI output token limit
- `spring.ai.google.genai.api-key`: from `GOOGLE_GENAI_API_KEY`
- `spring.ai.google.genai.chat.options.model`: default Gemini model
- `spring.ai.google.genai.chat.options.temperature`: default Gemini temperature
- `spring.ai.google.genai.chat.options.max-output-tokens`: default Gemini output token limit
- `app.ai.default-provider`: fallback provider when request has no `provider`

## Project Layout

```text
src/main/java/.../controller/AiChatController.java
src/main/java/.../dto/ChatRequest.java
src/main/java/.../service/AiChatService.java
src/main/java/.../service/AiChatServiceImpl.java
src/main/java/.../service/prompt/ChatPromptFactory.java
src/main/java/.../service/provider/ChatProvider.java
src/main/java/.../service/provider/ChatProviderRegistry.java
src/main/java/.../service/provider/OpenAiChatProvider.java
src/main/java/.../service/provider/GeminiChatProvider.java
src/main/resources/application.yaml
src/main/resources/static/index.html
src/main/resources/static/app.js
src/main/resources/static/styles.css
```

## Build

```powershell
mvn test
mvn clean package
```

## Notes

- OpenAI and Google may require billing or credits.
- Keep API keys in environment variables, not frontend code.
- Some models only support default temperature; use `1.0` for compatibility.
- For streaming in PowerShell, use `curl.exe`, not the `curl` alias.

## Links

[![YouTube](https://img.shields.io/badge/YouTube-ByteAndBeyondWithUday-red?logo=youtube&logoColor=white&style=flat-square)](https://www.youtube.com/@ByteAndBeyondWithUday)
[![Postman](https://img.shields.io/badge/Postman-Collection-orange?logo=postman&style=flat-square)](https://www.postman.com/planetary-water-884580/workspace/uday-s-public-workspace/folder/1581944-4b64b965-931e-4fa1-9786-9842f0f8f98a?action=share&source=copy-link&creator=1581944)
