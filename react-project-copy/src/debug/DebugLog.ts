export type DebugLogLevel = 'log' | 'warn' | 'error' | 'debug' | 'info';

export type DebugLogEntry = {
  ts: number;
  level: DebugLogLevel;
  message: string;
};

const MAX_ENTRIES = 4000;

let entries: DebugLogEntry[] = [];

const listeners = new Set<() => void>();

function notify(): void {
  for (const fn of listeners) {
    try {
      fn();
    } catch {
      /* ignore */
    }
  }
}

export function debugLogAppend(level: DebugLogLevel, message: string): void {
  entries.push({ ts: Date.now(), level, message });
  if (entries.length > MAX_ENTRIES) {
    entries = entries.slice(-MAX_ENTRIES);
  }
  notify();
}

export function debugLogSubscribe(listener: () => void): () => void {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}

export function debugLogGetSnapshot(): DebugLogEntry[] {
  return entries.slice();
}

export function debugLogClear(): void {
  entries = [];
  notify();
}

export function debugLogFormatExport(): string {
  const lines: string[] = [
    `Chitalka debug log export`,
    `Generated: ${new Date().toISOString()}`,
    `Entries: ${entries.length}`,
    '—'.repeat(40),
    '',
  ];
  for (const e of entries) {
    lines.push(`${new Date(e.ts).toISOString()}\t[${e.level}]\t${e.message}`);
  }
  return lines.join('\n');
}
