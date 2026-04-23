import type { DebugLogLevel } from './DebugLog';
import { debugLogAppend } from './DebugLog';

const GLOBAL_KEY = '__CHITALKA_CONSOLE_CAPTURE__';

function stringifyArg(value: unknown): string {
  if (typeof value === 'string') {
    return value;
  }
  if (value instanceof Error) {
    return `${value.name}: ${value.message}`;
  }
  try {
    return JSON.stringify(value);
  } catch {
    return String(value);
  }
}

function formatArgs(args: unknown[]): string {
  return args.map(stringifyArg).join(' ');
}

/**
 * Подключает перехват `console.*` в буфер логов (в т.ч. в release APK).
 * Идемпотентно: повторный импорт модуля не дублирует обёртки.
 */
export function installConsoleCapture(): void {
  const g = globalThis as typeof globalThis & { [GLOBAL_KEY]?: boolean };
  if (g[GLOBAL_KEY]) {
    return;
  }
  g[GLOBAL_KEY] = true;

  const wrap = (level: DebugLogLevel, orig: (...args: unknown[]) => void) => {
    return (...args: unknown[]) => {
      try {
        debugLogAppend(level, formatArgs(args));
      } catch {
        /* ignore */
      }
      orig(...args);
    };
  };

  /* eslint-disable no-console */
  const origLog = console.log.bind(console);
  const origInfo = console.info.bind(console);
  const origWarn = console.warn.bind(console);
  const origError = console.error.bind(console);
  const origDebug = console.debug.bind(console);

  console.log = wrap('log', origLog);
  console.info = wrap('info', origInfo);
  console.warn = wrap('warn', origWarn);
  console.error = wrap('error', origError);
  console.debug = wrap('debug', origDebug);
  /* eslint-enable no-console */
}

installConsoleCapture();
