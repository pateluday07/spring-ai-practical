const form = document.querySelector("#chatForm");
const promptInput = document.querySelector("#prompt");
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
    modelInput.disabled = isBusy;
    maxTokensInput.disabled = isBusy;
    temperatureInput.disabled = isBusy;
}

function resetOutput() {
    chunkCount = 0;
    rawResponse = "";
    responseOutput.textContent = "";
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
    return {
        prompt: promptInput.value.trim(),
        model: modelInput.value.trim() || null,
        maxCompletionTokens: Number(maxTokensInput.value),
        temperature: Number(temperatureInput.value)
    };
}

async function streamResponse(requestBody) {
    controller = new AbortController();
    startedAt = performance.now();
    timer = window.setInterval(updateElapsed, 100);
    updateElapsed();

    const response = await fetch("/api/ai/chat/stream-text", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Accept": "text/plain"
        },
        body: JSON.stringify(requestBody),
        signal: controller.signal
    });

    if (!response.ok || !response.body) {
        throw new Error(`Request failed with HTTP ${response.status}`);
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();

    while (true) {
        const { value, done } = await reader.read();

        if (done) {
            break;
        }

        appendChunk(decoder.decode(value, { stream: true }));
    }

    const finalText = decoder.decode();
    appendChunk(finalText);
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
