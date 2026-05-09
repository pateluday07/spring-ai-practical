# Spring AI Practical Demo - Whiteboard Notes

## 1. Create the AI Project

Go to Spring Initializr:

```text
https://start.spring.io
```

Select these options:

```text
Project: Maven
Language: Java
Spring Boot: latest stable version
Java: 21
Packaging: Jar
```

Add dependencies:

```text
Spring Web
OpenAI / Spring AI OpenAI
```

If OpenAI dependency is not available directly in Spring Initializr, add it manually in `pom.xml`.

Project goal:

```text
User sends a message -> Spring Boot API receives it -> Spring AI sends it to OpenAI -> API returns AI answer as JSON
```

## 2. Explain pom.xml

`pom.xml` is the Maven configuration file.

It tells Maven:

- Which Java version we are using
- Which Spring Boot version we are using
- Which dependencies are needed
- How to build and run the project

Important part:

```xml
<properties>
    <java.version>21</java.version>
    <spring-ai.version>1.1.5</spring-ai.version>
</properties>
```

Meaning:

- Project uses Java 21
- Project uses Spring AI version 1.1.5

Spring Web dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

Meaning:

- This helps us create REST APIs
- It gives annotations like `@RestController`, `@PostMapping`, and `@RequestBody`

Spring AI OpenAI dependency:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

Meaning:

- This connects our Spring Boot app with OpenAI
- It creates the `ChatModel` bean automatically
- We can inject `ChatModel` in our service layer

Spring AI BOM:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>${spring-ai.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Meaning:

- BOM means Bill of Materials
- It manages compatible Spring AI dependency versions
- Because of this, we do not need to write version number for every Spring AI dependency

## 3. Explain application.yaml

`application.yaml` contains application configuration.

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

```yaml
spring:
  application:
    name: spring-ai-practical
```

This is the application name.

```yaml
spring:
  ai:
    model:
      chat: openai
```

This tells Spring AI:

```text
Use OpenAI as the chat model provider.
```

```yaml
api-key: ${OPENAI_API_KEY:add-your-api-key-here}
```

This tells Spring Boot:

```text
First, read API key from environment variable OPENAI_API_KEY.
If it is not present, use add-your-api-key-here as default value.
```

For real projects, do not keep the API key directly in code.

Create OpenAI API key from:

```text
https://platform.openai.com/api-keys
```

Steps:

1. Login to OpenAI Platform
2. Go to API keys page
3. Create a new API key
4. Copy the key
5. Store it in environment variable

In Windows PowerShell, temporary setup:

```powershell
$env:OPENAI_API_KEY = "your-api-key-here"
```

Then run the app from the same PowerShell:

```powershell
mvn spring-boot:run
```

Model config:

```yaml
model: ${OPENAI_CHAT_MODEL:gpt-4o-mini}
```

Meaning:

- Use environment variable `OPENAI_CHAT_MODEL` if available
- Otherwise use `gpt-4o-mini`

Temperature config:

```yaml
temperature: ${OPENAI_TEMPERATURE:0.7}
```

Meaning:

- Temperature controls creativity
- `0.0` means more strict and predictable
- `0.7` means balanced
- Higher value means more creative/random

Token config:

```yaml
max-tokens: ${OPENAI_MAX_TOKENS:500}
```

Meaning:

- It controls maximum response length
- Around 500 tokens usually means around 350 to 400 English words
- The AI can still answer shorter if the question is simple

Important note:

```text
OpenAI API is not fully free. You may need credits or billing.
For a completely free local demo, use Ollama instead of OpenAI.
```

## 4. Explain Service Layer

Service interface:

```java
public interface AiChatService {

    String chat(String message);

}
```

Meaning:

- This defines what our chat service can do
- It accepts a user message
- It returns the AI answer as a string

Service implementation:

```java
@Service
public class AiChatServiceImpl implements AiChatService {

    private final ChatModel chatModel;

    public AiChatServiceImpl(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String chat(String message) {
        return chatModel.call(message);
    }
}
```

Explanation:

```java
private final ChatModel chatModel;
```

`ChatModel` is provided by Spring AI.

It knows how to call the configured AI provider, which is OpenAI in our project.

```java
public AiChatServiceImpl(ChatModel chatModel) {
    this.chatModel = chatModel;
}
```

This is constructor injection.

Spring Boot automatically provides the `ChatModel` object.

```java
return chatModel.call(message);
```

This line sends the user message to OpenAI and returns the AI response.

Service layer responsibility:

```text
Controller should not directly talk to AI.
Controller calls service.
Service talks to Spring AI.
```

## 5. Explain Controller Layer

Controller:

```java
@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiChatService chatService;

    public AiChatController(AiChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> chat(@RequestBody String message) {
        return Map.of("answer", chatService.chat(message));
    }
}
```

Explanation:

```java
@RestController
```

This class handles REST API requests.

```java
@RequestMapping("/api/ai")
```

Base URL for this controller.

```java
@PostMapping("/chat")
```

Final API endpoint:

```text
POST /api/ai/chat
```

```java
@RequestBody String message
```

This means request body can be only plain text.

Example request body:

```text
Explain Spring Boot in simple words
```

No request DTO is required because we are sending only one value.

```java
return Map.of("answer", chatService.chat(message));
```

This returns JSON without creating a DTO.

Example response:

```json
{
  "answer": "Spring Boot is a framework that helps you create Java applications quickly..."
}
```

Controller layer responsibility:

```text
Receive HTTP request -> call service -> return HTTP response
```

## 6. Demo Time

Start the application:

```powershell
mvn spring-boot:run
```

Test the API using Postman:

```text
Method: POST
URL: http://localhost:8080/api/ai/chat
Header: Content-Type = text/plain
Body:
Explain Spring Boot in simple language
```

Expected response:

```json
{
  "answer": "Spring Boot is a tool that helps Java developers build applications quickly..."
}
```

Test using curl:

```powershell
curl -X POST "http://localhost:8080/api/ai/chat" `
  -H "Content-Type: text/plain" `
  -d "Explain Spring AI in simple words"
```

Flow recap:

```text
Postman/curl sends message
        |
        v
AiChatController receives request
        |
        v
AiChatServiceImpl calls ChatModel
        |
        v
Spring AI calls OpenAI
        |
        v
Controller returns JSON response
```

Final summary for viewers:

```text
We created a Spring Boot REST API.
We added Spring AI OpenAI dependency.
We configured OpenAI in application.yaml.
We created a service layer to call AI.
We created a controller layer to expose an API.
Finally, we tested the API and received AI response as JSON.
```
