import { TOKEN_KEY } from "../api/client";
import type { AccessRecord } from "../types";

export type StreamStatus = "connected" | "reconnecting" | "disconnected";

interface StreamOptions {
  onAccess: (access: AccessRecord) => void;
  onStatus: (status: StreamStatus) => void;
}

const RETRY_DELAYS = [1_000, 2_000, 5_000, 10_000];

function streamUrl() {
  const baseUrl = (import.meta.env.VITE_API_URL || "").replace(/\/$/, "");
  return `${baseUrl}/api/accesos/stream`;
}

function unauthorized() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem("planprocons_username");
  if (window.location.pathname !== "/login") window.location.assign("/login");
}

export function openAccessStream({ onAccess, onStatus }: StreamOptions) {
  const controller = new AbortController();
  let stopped = false;
  let retryIndex = 0;
  let retryTimer: number | undefined;

  const processEvent = (block: string) => {
    let eventName = "message";
    const data: string[] = [];
    for (const line of block.split("\n")) {
      if (line.startsWith("event:")) eventName = line.slice(6).trim();
      if (line.startsWith("data:")) data.push(line.slice(5).trimStart());
    }
    if (eventName === "connected") {
      retryIndex = 0;
      onStatus("connected");
    } else if (eventName === "access-created" && data.length) {
      try {
        onAccess(JSON.parse(data.join("\n")) as AccessRecord);
      } catch {
        // Un evento malformado se ignora sin cerrar el canal.
      }
    }
  };

  const connect = async () => {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token || stopped) {
      onStatus("disconnected");
      return;
    }
    try {
      const response = await fetch(streamUrl(), {
        headers: { Accept: "text/event-stream", Authorization: `Bearer ${token}` },
        cache: "no-store",
        signal: controller.signal,
      });
      if (response.status === 401 || response.status === 403) {
        stopped = true;
        onStatus("disconnected");
        unauthorized();
        return;
      }
      if (!response.ok || !response.body) throw new Error(`SSE HTTP ${response.status}`);
      onStatus("connected");
      retryIndex = 0;
      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = "";
      while (!stopped) {
        const { done, value } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true }).replaceAll("\r\n", "\n");
        let boundary = buffer.indexOf("\n\n");
        while (boundary >= 0) {
          processEvent(buffer.slice(0, boundary));
          buffer = buffer.slice(boundary + 2);
          boundary = buffer.indexOf("\n\n");
        }
      }
      if (!stopped) throw new Error("SSE cerrado");
    } catch (error) {
      if (stopped || (error instanceof DOMException && error.name === "AbortError")) return;
      onStatus("reconnecting");
      const delay = RETRY_DELAYS[Math.min(retryIndex, RETRY_DELAYS.length - 1)];
      retryIndex += 1;
      retryTimer = window.setTimeout(() => void connect(), delay);
    }
  };

  void connect();
  return () => {
    stopped = true;
    if (retryTimer !== undefined) window.clearTimeout(retryTimer);
    controller.abort();
    onStatus("disconnected");
  };
}
