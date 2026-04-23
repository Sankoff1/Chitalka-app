import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react';

import {
  type ThemeColors,
  type ThemeMode,
  getColorsForMode,
} from './colors';

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
  const [mode, setMode] = useState<ThemeMode>(initialMode);

  const colors = useMemo(() => getColorsForMode(mode), [mode]);

  const toggleTheme = useCallback(() => {
    setMode((m) => (m === 'light' ? 'dark' : 'light'));
  }, []);

  const value = useMemo(
    () => ({
      mode,
      colors,
      setMode,
      toggleTheme,
    }),
    [colors, mode, toggleTheme]
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
