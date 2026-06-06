const form = document.querySelector("#chatForm");
const promptInput = document.querySelector("#prompt");
const systemInput = document.querySelector("#system");
const providerInput = document.querySelector("#provider");
const modelInput = document.querySelector("#model");
const maxTokensInput = document.querySelector("#maxCompletionTokens");
const temperatureInput = document.querySelector("#temperature");
const sendButton = document.querySelector("#sendButton");
const stopButton = document.querySelector("#stopButton");
const clearButton = document.querySelector("#clearButton");
const responseOutput = document.querySelector("#responseOutput");
const chunkList = document.querySelector("#chunkList");
const tokenCounter = document.querySelector("#tokenCounter");
const elapsedTime = document.querySelector("#elapsedTime");
const statusText = document.querySelector("#statusText");
const statusPill = document.querySelector("#statusPill");

let controller = null;
let chunkCount = 0;
let startedAt = 0;
let timer = null;
let rawResponse = "";

const providerDefaults = {
    openai: "gpt-4o-mini",
    gemini: "gemini-2.5-flash"
};

if (window.marked) {
    window.marked.use({
        gfm: true,
        breaks: true
    });
}

function setStatus(label, detail, state) {
    statusPill.textContent = label;
    statusText.textContent = detail;
    statusPill.classList.toggle("streaming", state === "streaming");
    statusPill.classList.toggle("error", state === "error");
}

function setBusy(isBusy) {
    sendButton.disabled = isBusy;
    stopButton.disabled = !isBusy;
    promptInput.disabled = isBusy;
    systemInput.disabled = isBusy;
    providerInput.disabled = isBusy;
    modelInput.disabled = isBusy;
    maxTokensInput.disabled = isBusy;
    temperatureInput.disabled = isBusy;
}

function resetOutput() {
    chunkCount = 0;
    rawResponse = "";
    responseOutput.replaceChildren();
    chunkList.replaceChildren();
    tokenCounter.textContent = "0 chunks";
    elapsedTime.textContent = "0.0s";
}

function updateElapsed() {
    if (!startedAt) {
        elapsedTime.textContent = "0.0s";
        return;
    }

    elapsedTime.textContent = `${((performance.now() - startedAt) / 1000).toFixed(1)}s`;
}

function appendChunk(text) {
    if (!text) {
        return;
    }

    chunkCount += 1;
    rawResponse += text;
    renderResponse();
    responseOutput.scrollTop = responseOutput.scrollHeight;
    tokenCounter.textContent = `${chunkCount} ${chunkCount === 1 ? "chunk" : "chunks"}`;

    const item = document.createElement("li");
    const index = document.createElement("span");
    const value = document.createElement("span");

    index.className = "chunk-index";
    value.className = "chunk-text";
    index.textContent = `#${chunkCount}`;
    value.textContent = text.replace(/\n/g, "\\n");

    item.append(index, value);
    chunkList.append(item);
    chunkList.scrollTop = chunkList.scrollHeight;
}

function renderResponse() {
    if (!rawResponse) {
        responseOutput.replaceChildren();
        return;
    }

    if (!window.marked || !window.DOMPurify) {
        responseOutput.textContent = rawResponse;
        return;
    }

    const html = window.marked.parse(rawResponse);
    responseOutput.innerHTML = window.DOMPurify.sanitize(html);
}

function buildRequestBody() {
    const requestBody = {
        prompt: promptInput.value.trim(),
        provider: providerInput.value,
        model: modelInput.value.trim() || null,
        maxCompletionTokens: Number(maxTokensInput.value),
        temperature: Number(temperatureInput.value)
    };

    const system = systemInput.value.trim();

    if (system) {
        requestBody.system = system;
    }

    return requestBody;
}

async function streamResponse(requestBody) {
    controller = new AbortController();
    startedAt = performance.now();
    timer = window.setInterval(updateElapsed, 100);
    updateElapsed();

    const response = await fetch("/api/ai/chat/stream", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Accept": "text/event-stream"
        },
        body: JSON.stringify(requestBody),
        signal: controller.signal
    });

    if (!response.ok || !response.body) {
        throw new Error(`Request failed with HTTP ${response.status}`);
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let sseBuffer = "";

    while (true) {
        const { value, done } = await reader.read();

        if (done) {
            break;
        }

        sseBuffer = parseSseBuffer(sseBuffer + decoder.decode(value, { stream: true }));
    }

    sseBuffer = parseSseBuffer(sseBuffer + decoder.decode());

    if (sseBuffer.trim()) {
        appendSseEvent(sseBuffer);
    }
}

function parseSseBuffer(buffer) {
    const normalized = buffer.replace(/\r\n/g, "\n");
    const events = normalized.split("\n\n");
    const pending = events.pop() || "";

    events.forEach(appendSseEvent);
    return pending;
}

function appendSseEvent(eventBlock) {
    const dataLines = eventBlock
        .split("\n")
        .filter(line => line.startsWith("data:"))
        .map(line => line.slice(5));

    if (dataLines.length === 0) {
        return;
    }

    const data = dataLines.join("\n");

    if (data === "[DONE]") {
        return;
    }

    appendChunk(data);
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const requestBody = buildRequestBody();

    if (!requestBody.prompt) {
        setStatus("Missing", "Enter a prompt before sending.", "error");
        promptInput.focus();
        return;
    }

    resetOutput();
    setBusy(true);
    setStatus("Streaming", "Receiving response chunks.", "streaming");

    try {
        await streamResponse(requestBody);
        setStatus("Complete", `Finished in ${elapsedTime.textContent}.`, "idle");
    } catch (error) {
        if (error.name === "AbortError") {
            setStatus("Stopped", "The stream was stopped.", "idle");
        } else {
            appendChunk(`\n\nError: ${error.message}`);
            setStatus("Error", error.message, "error");
        }
    } finally {
        if (timer) {
            window.clearInterval(timer);
            timer = null;
        }

        updateElapsed();
        setBusy(false);
        controller = null;
    }
});

stopButton.addEventListener("click", () => {
    if (controller) {
        controller.abort();
    }
});

clearButton.addEventListener("click", () => {
    resetOutput();
    setStatus("Idle", "Ready", "idle");
});

providerInput.addEventListener("change", () => {
    modelInput.value = providerDefaults[providerInput.value] || "";
});
