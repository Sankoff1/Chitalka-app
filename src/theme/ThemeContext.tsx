import AsyncStorage from '@react-native-async-storage/async-storage';
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';

import {
  type ThemeColors,
  type ThemeMode,
  getColorsForMode,
} from './colors';

const THEME_MODE_STORAGE_KEY = 'chitalka_theme_mode';

type ThemeContextValue = {
  mode: ThemeMode;
  colors: ThemeColors;
  setMode: (mode: ThemeMode) => void;
  toggleTheme: () => void;
};

const ThemeContext = createContext<ThemeContextValue | null>(null);

type ThemeProviderProps = {
  children: ReactNode;
  initialMode?: ThemeMode;
};

export function ThemeProvider({
  children,
  initialMode = 'light',
}: ThemeProviderProps) {
  const [mode, setModeState] = useState<ThemeMode>(initialMode);

  useEffect(() => {
    let cancelled = false;
    void (async () => {
      try {
        const stored = await AsyncStorage.getItem(THEME_MODE_STORAGE_KEY);
        console.log('[theme] hydrate read', { stored });
        if (!cancelled && (stored === 'light' || stored === 'dark')) {
          setModeState(stored);
        }
      } catch (err) {
        console.log('[theme] hydrate error', err);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const persistMode = useCallback(async (next: ThemeMode) => {
    try {
      await AsyncStorage.setItem(THEME_MODE_STORAGE_KEY, next);
      console.log('[theme] persist ok', { next });
    } catch (err) {
      console.log('[theme] persist error', err);
    }
  }, []);

  const setMode = useCallback(
    (next: ThemeMode) => {
      setModeState(next);
      void persistMode(next);
    },
    [persistMode]
  );

  /** Ссылки на палитры статичны (colors.ts) — без лишнего useMemo на каждый рендер. */
  const colors = getColorsForMode(mode);

  const toggleTheme = useCallback(() => {
    setModeState((m) => {
      const next = m === 'light' ? 'dark' : 'light';
      void persistMode(next);
      return next;
    });
  }, [persistMode]);

  const value = useMemo(
    () => ({
      mode,
      colors,
      setMode,
      toggleTheme,
    }),
    [colors, mode, setMode, toggleTheme]
  );

  return (
    <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>
  );
}

export function useTheme(): ThemeContextValue {
  const ctx = useContext(ThemeContext);
  if (!ctx) {
    throw new Error('useTheme must be used within ThemeProvider');
  }
  return ctx;
}
