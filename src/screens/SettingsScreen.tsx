import Constants from 'expo-constants';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { APP_LOCALES, useI18n, type AppLocale } from '../i18n';
import { useTheme } from '../theme';

function resolveAppVersion(): string {
  return (
    Constants.expoConfig?.version ??
    Constants.nativeApplicationVersion ??
    '—'
  );
}

export function SettingsScreen() {
  const { mode, colors, setMode } = useTheme();
  const { locale, setLocale, t } = useI18n();
  const version = resolveAppVersion();

  const setLanguage = (next: AppLocale) => {
    setLocale(next);
  };

  return (
    <View style={[styles.root, { backgroundColor: colors.background }]}>
      <Text style={[styles.title, { color: colors.text }]}>
        {t('settings.title')}
      </Text>

      <Text style={[styles.sectionLabel, { color: colors.textSecondary }]}>
        {t('settings.themeSection')}
      </Text>
      <View style={styles.row}>
        <Pressable
          onPress={() => setMode('light')}
          style={({ pressed }) => [
            styles.chip,
            { backgroundColor: colors.interactive },
            mode === 'light' && styles.chipActive,
            pressed && styles.chipPressed,
          ]}
        >
          <Text style={[styles.chipText, { color: colors.text }]}>
            {t('settings.themeLight')}
          </Text>
        </Pressable>
        <Pressable
          onPress={() => setMode('dark')}
          style={({ pressed }) => [
            styles.chip,
            { backgroundColor: colors.interactive },
            mode === 'dark' && styles.chipActive,
            pressed && styles.chipPressed,
          ]}
        >
          <Text style={[styles.chipText, { color: colors.text }]}>
            {t('settings.themeDark')}
          </Text>
        </Pressable>
      </View>

      <Text
        style={[
          styles.sectionLabel,
          styles.sectionSpaced,
          { color: colors.textSecondary },
        ]}
      >
        {t('settings.languageSection')}
      </Text>
      <View style={styles.row}>
        {APP_LOCALES.map((code) => (
          <Pressable
            key={code}
            onPress={() => setLanguage(code)}
            style={({ pressed }) => [
              styles.chip,
              { backgroundColor: colors.interactive },
              locale === code && styles.chipActive,
              pressed && styles.chipPressed,
            ]}
          >
            <Text style={[styles.chipText, { color: colors.text }]}>
              {code === 'ru'
                ? t('settings.languageRu')
                : t('settings.languageEn')}
            </Text>
          </Pressable>
        ))}
      </View>

      <View style={[styles.versionBlock, { borderTopColor: colors.textSecondary + '33' }]}>
        <Text style={[styles.versionLabel, { color: colors.textSecondary }]}>
          {t('settings.versionLabel')}
        </Text>
        <Text style={[styles.versionValue, { color: colors.text }]}>{version}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    paddingHorizontal: 24,
    paddingTop: 24,
  },
  title: {
    fontSize: 22,
    fontWeight: '700',
    marginBottom: 24,
  },
  sectionLabel: {
    fontSize: 15,
    marginBottom: 12,
  },
  sectionSpaced: {
    marginTop: 8,
  },
  row: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
    marginBottom: 8,
  },
  chip: {
    flexGrow: 1,
    flexShrink: 1,
    flexBasis: 0,
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 12,
    paddingHorizontal: 18,
    borderRadius: 10,
    borderWidth: 2,
    borderColor: 'transparent',
  },
  chipActive: {
    borderColor: 'rgba(0,0,0,0.25)',
  },
  chipPressed: {
    opacity: 0.88,
  },
  chipText: {
    fontSize: 16,
    fontWeight: '600',
    textAlign: 'center',
  },
  versionBlock: {
    marginTop: 32,
    paddingTop: 20,
    borderTopWidth: StyleSheet.hairlineWidth,
  },
  versionLabel: {
    fontSize: 14,
    marginBottom: 6,
  },
  versionValue: {
    fontSize: 18,
    fontWeight: '600',
  },
});
