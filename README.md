# Spring AI Practical

Spring Boot app that sends chat requests to OpenAI through Spring AI. It supports both a normal JSON chat API and a streaming API that sends response chunks as Server-Sent Events.

**Flow:** Client -> `AiChatController` -> `AiChatServiceImpl` -> Spring AI `ChatModel` -> OpenAI -> JSON or streamed Markdown response

**Stack:** Java 21, Spring Boot 3.5.14, Spring AI 1.1.5, OpenAI, Maven

## Quick Start

1. Get an API key: https://platform.openai.com/api-keys
2. Set env vars in the same terminal session:

```powershell
$env:OPENAI_API_KEY = "your-api-key-here"
# optional: OPENAI_CHAT_MODEL, OPENAI_TEMPERATURE, OPENAI_MAX_COMPLETION_TOKENS, SPRING_AI_SYSTEM
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
  "prompt": "How does Kubernetes handle self-healing?"
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

```json
{
  "prompt": "How does Kubernetes handle self-healing?",
  "model": "gpt-5.4-nano",
  "maxCompletionTokens": 1000,
  "temperature": 1
}
```

PowerShell curl test:

```powershell
curl.exe -N --location "http://localhost:8080/api/ai/chat/stream" `
  --header "Content-Type: application/json" `
  --header "Accept: text/event-stream" `
  --data-raw '{"prompt":"How does Kubernetes handle self-healing?","model":"gpt-5.4-nano","maxCompletionTokens":1000,"temperature":1}'
```

The `-N` flag disables curl buffering so chunks appear as they arrive.

## Request Fields

All fields except `prompt` are optional.

| Field | Description |
|-------|-------------|
| `prompt` | User message |
| `system` | Optional system instruction override |
| `model` | Optional model override |
| `temperature` | Optional randomness setting |
| `maxCompletionTokens` | Optional max output token setting |

When `system` is omitted or blank, the backend uses `spring.ai.system`.

## Streaming Implementation

The controller exposes the stream as:

```java
@PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> stream(@RequestBody ChatRequest request) {
    return chatService.stream(request);
}
```

The service uses Spring AI streaming:

```java
return chatModel.stream(prompt)
        .mapNotNull(response -> {
            if (response.getResult() == null) {
                return null;
            }
            return response.getResult().getOutput().getText();
        })
        .filter(text -> !text.isEmpty());
```

The frontend calls the streaming endpoint with `fetch()`, reads `response.body.getReader()`, parses SSE `data:` chunks, and re-renders Markdown as text arrives.

## Configuration

See `src/main/resources/application.yaml`.

Key settings:

- `spring.ai.system`: default system prompt
- `spring.ai.openai.api-key`: from `OPENAI_API_KEY`
- `spring.ai.openai.chat.options.model`: default model
- `spring.ai.openai.chat.options.temperature`: default temperature
- `spring.ai.openai.chat.options.max-completion-tokens`: default output token limit

## Project Layout

```text
src/main/java/.../controller/AiChatController.java
src/main/java/.../dto/ChatRequest.java
src/main/java/.../service/AiChatService.java
src/main/java/.../service/AiChatServiceImpl.java
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

- OpenAI may require billing or credits.
- Keep API keys in environment variables, not frontend code.
- Some models only support default temperature; use `1.0` for compatibility.
- For streaming in PowerShell, use `curl.exe`, not the `curl` alias.

## Links

[![YouTube](https://img.shields.io/badge/YouTube-ByteAndBeyondWithUday-red?logo=youtube&logoColor=white&style=flat-square)](https://www.youtube.com/@ByteAndBeyondWithUday)
[![Postman](https://img.shields.io/badge/Postman-Collection-orange?logo=postman&style=flat-square)](https://www.postman.com/planetary-water-884580/workspace/uday-s-public-workspace/request/1581944-c2ec32af-7bcc-49f2-9ec0-165d59a8f021?action=share&source=copy-link&creator=1581944)
