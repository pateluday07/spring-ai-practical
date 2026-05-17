# Spring AI Practical

Spring Boot REST API that sends structured chat requests to OpenAI via Spring AI and returns Markdown responses as JSON.

**Flow:** Client → `AiChatController` → `AiChatServiceImpl` (Prompt + `OpenAiChatOptions`) → Spring AI `ChatModel` → OpenAI → `{ "response": "..." }`

**Stack:** Java 21 · Spring Boot 3.5.14 · Spring AI 1.1.5 · OpenAI · Maven

## Quick Start

1. Get an API key: https://platform.openai.com/api-keys
2. Set env vars (PowerShell, same session):

```powershell
$env:OPENAI_API_KEY = "your-api-key-here"
# optional: OPENAI_CHAT_MODEL, OPENAI_TEMPERATURE, OPENAI_MAX_COMPLETION_TOKENS, SPRING_AI_SYSTEM
```

3. Run: `mvn spring-boot:run` → http://localhost:8080

Do not hardcode secrets in `application.yaml`.

## API

`POST /api/ai/chat` · `Content-Type: application/json`

**Request** — all fields except `prompt` are optional (YAML defaults apply when omitted):

| Field | Description |
|-------|-------------|
| `prompt` | User message |
| `system` | System instruction (default: `spring.ai.system`) |
| `model` | Model override |
| `temperature` | Randomness |
| `maxCompletionTokens` | Max output tokens |

```json
{ "prompt": "What is horsepower?" }
```

```json
{
  "prompt": "What is horsepower?",
  "system": "You are a helpful assistant.",
  "model": "gpt-4o-mini",
  "temperature": 1.0,
  "maxCompletionTokens": 300
}
```

**Response:** `{ "response": "..." }` (Markdown by default)

**Test (PowerShell):**

```powershell
curl -X POST "http://localhost:8080/api/ai/chat" `
  -H "Content-Type: application/json" `
  -d '{ "prompt": "What is horsepower?" }'
```

## Configuration

See `src/main/resources/application.yaml`. Key settings:

- `spring.ai.system` — default system prompt
- `spring.ai.openai.api-key` — from `OPENAI_API_KEY`
- `spring.ai.openai.chat.options` — `model`, `temperature`, `max-completion-tokens`

## Project Layout

```text
controller/AiChatController.java
dto/ChatRequest.java
service/AiChatService.java, AiChatServiceImpl.java
SpringAiPracticalApplication.java
application.yaml
```

Request overrides (`model`, `temperature`, `maxCompletionTokens`) build `OpenAiChatOptions` only when present; otherwise YAML defaults are used.

## Build

```powershell
mvn test
mvn clean package
```

## Notes

- OpenAI may require billing/credits.
- Keep API keys in env vars, not frontend code.
- Some models only support default temperature — use `1.0` for compatibility.

## Links

[![YouTube](https://img.shields.io/badge/YouTube-ByteAndBeyondWithUday-red?logo=youtube&logoColor=white&style=flat-square)](https://www.youtube.com/@ByteAndBeyondWithUday)
[![Postman](https://img.shields.io/badge/Postman-Collection-orange?logo=postman&style=flat-square)](https://www.postman.com/planetary-water-884580/workspace/uday-s-public-workspace/folder/1581944-4ffcb36e-0f2d-4e93-8f7d-986000761292?action=share&source=copy-link&creator=1581944)
