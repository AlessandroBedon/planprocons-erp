import { useEffect, useRef, useState } from "react";
import { openAccessStream, type StreamStatus } from "../services/accessStream";
import type { AccessRecord } from "../types";

export function useAccessStream(onAccess: (access: AccessRecord) => void) {
  const callbackRef = useRef(onAccess);
  const [status, setStatus] = useState<StreamStatus>("reconnecting");

  useEffect(() => {
    callbackRef.current = onAccess;
  }, [onAccess]);

  useEffect(() => openAccessStream({
    onAccess: (access) => callbackRef.current(access),
    onStatus: setStatus,
  }), []);

  return status;
}
