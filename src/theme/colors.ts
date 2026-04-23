export type ThemeMode = 'light' | 'dark';

export type ThemeColors = {
  /** Основной задник экрана */
  background: string;
  /** Кликабельные элементы (кнопки, акценты) */
  interactive: string;
  /** Верхняя панель (header) */
  topBar: string;
  /** Фон бокового меню */
  menuBackground: string;
  /** Текст на топ-баре */
  topBarText: string;
  /** Основной текст контента */
  text: string;
  /** Второстепенный текст */
  textSecondary: string;
};

export const lightThemeColors: ThemeColors = {
  background: '#EBFADD',
  interactive: '#9FDE75',
  topBar: '#2A7833',
  menuBackground: '#F8FCEE',
  topBarText: '#FFFFFF',
  text: '#1A2E1C',
  textSecondary: '#4A5F4C',
};

export const darkThemeColors: ThemeColors = {
  background: '#172016',
  interactive: '#39513A',
  topBar: '#00480A',
  menuBackground: '#222C20',
  topBarText: '#FFFFFF',
  text: '#E6F0E2',
  textSecondary: '#A8B8A5',
};

export function getColorsForMode(mode: ThemeMode): ThemeColors {
  return mode === 'dark' ? darkThemeColors : lightThemeColors;
}
