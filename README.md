# Spring AI Practical

Simple Spring Boot REST API that sends a user message to OpenAI using Spring AI and returns the AI response as JSON.

## What This Project Does

Flow:

```text
User sends message
    -> Spring Boot controller receives request
    -> Service layer calls Spring AI ChatModel
    -> Spring AI sends request to OpenAI
    -> API returns AI answer as JSON
```

Example response:

```json
{
  "answer": "Spring Boot is a framework that helps you build Java applications quickly..."
}
```

## Tech Stack

- Java 21
- Spring Boot 3.5.14
- Spring AI 1.1.5
- OpenAI Chat Model
- Maven

## Project Structure

```text
src/main/java/com/bbu/springai/springaipractical
  controller
    AiChatController.java
  service
    AiChatService.java
    AiChatServiceImpl.java
  SpringAiPracticalApplication.java

src/main/resources
  application.yaml
```

## Dependencies

Main dependencies in `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

Meaning:

- `spring-boot-starter-web` is used to create REST APIs.
- `spring-ai-starter-model-openai` is used to connect Spring Boot with OpenAI.

## Configuration

Configuration is available in:

```text
src/main/resources/application.yaml
```

Current config:

```yaml
spring:
  application:
    name: spring-ai-practical
  ai:
    model:
      chat: openai
    openai:
      api-key: ${OPENAI_API_KEY:add-your-api-key-here}
      chat:
        options:
          model: ${OPENAI_CHAT_MODEL:gpt-4o-mini}
          temperature: ${OPENAI_TEMPERATURE:0.7}
          max-tokens: ${OPENAI_MAX_TOKENS:500}
```

Explanation:

- `spring.ai.model.chat: openai` tells Spring AI to use OpenAI for chat.
- `api-key` reads the OpenAI API key from the environment variable `OPENAI_API_KEY`.
- `model` is the OpenAI model name. Default is `gpt-4o-mini`.
- `temperature` controls creativity. Lower value gives more predictable answers.
- `max-tokens` controls the maximum response length.

## OpenAI API Key

Create an API key from:

```text
https://platform.openai.com/api-keys
```

Do not hardcode your real API key inside `application.yaml`.

Set the API key using an environment variable.

Windows PowerShell:

```powershell
$env:OPENAI_API_KEY = "your-api-key-here"
```

Optional environment variables:

```powershell
$env:OPENAI_CHAT_MODEL = "gpt-4o-mini"
$env:OPENAI_TEMPERATURE = "0.7"
$env:OPENAI_MAX_TOKENS = "500"
```

These values work only in the same PowerShell window where you set them.

## Run The Application

Start the app:

```powershell
mvn spring-boot:run
```

Default server URL:

```text
http://localhost:8080
```

## API Endpoint

### Chat API

```text
POST /api/ai/chat
```

Full URL:

```text
http://localhost:8080/api/ai/chat
```

Request body type:

```text
text/plain
```

Example request body:

```text
Explain Spring Boot in simple words
```

Example JSON response:

```json
{
  "answer": "Spring Boot is a Java framework that helps developers create applications quickly..."
}
```

## Test With curl

Windows PowerShell:

```powershell
curl -X POST "http://localhost:8080/api/ai/chat" `
  -H "Content-Type: text/plain" `
  -d "Explain Spring AI in simple words"
```

## Test With Postman

Use these values:

```text
Method: POST
URL: http://localhost:8080/api/ai/chat
Header: Content-Type = text/plain
Body: Explain Spring AI in simple words
```

## Controller Layer

`AiChatController` receives the HTTP request.

```java
@PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
public Map<String, String> chat(@RequestBody String message) {
    return Map.of("answer", chatService.chat(message));
}
```

Meaning:

- Request body is a plain string.
- No request DTO is needed.
- Response is returned as JSON using `Map`.

## Service Layer

`AiChatServiceImpl` calls Spring AI.

```java
@Override
public String chat(String message) {
    return chatModel.call(message);
}
```

Meaning:

- `ChatModel` is provided by Spring AI.
- `chatModel.call(message)` sends the message to OpenAI.
- The AI response is returned as a string.

## Important Notes

- OpenAI API may require credits or billing.
- Keep your API key secret.
- Do not expose API keys in frontend code.
- Use environment variables for secrets.
- For a completely free local demo, you can use Ollama instead of OpenAI, but that requires changing the dependency and configuration.

## Build And Test

Run tests:

```powershell
mvn test
```

Build the project:

```powershell
mvn clean package
```

## Summary

This project is a simple Spring AI demo:

```text
Spring Boot REST API + Spring AI + OpenAI
```

It accepts a plain text message and returns the AI-generated answer as JSON.
