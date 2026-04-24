import MaterialIcons from '@expo/vector-icons/MaterialIcons';
import Constants from 'expo-constants';
import { useCallback, useMemo, useRef, useState } from 'react';
import {
  Dimensions,
  Modal,
  Platform,
  Pressable,
  StyleSheet,
  Switch,
  Text,
  View,
} from 'react-native';

import { APP_LOCALES, useI18n, type AppLocale } from '../i18n';
import { useTheme } from '../theme';

/** Высота панели: две строки ~48px + разделитель. */
const LANGUAGE_PANEL_ESTIMATE = 102;

function resolveAppVersion(): string {
  return (
    Constants.expoConfig?.version ??
    Constants.nativeApplicationVersion ??
    '—'
  );
}

function localeLabel(code: AppLocale, t: (path: string) => string): string {
  return code === 'ru' ? t('settings.languageRu') : t('settings.languageEn');
}

type AnchorRect = {
  x: number;
  y: number;
  width: number;
  height: number;
  openAbove: boolean;
};

export function SettingsScreen() {
  const { mode, colors, setMode } = useTheme();
  const { locale, setLocale, t } = useI18n();
  const version = resolveAppVersion();
  const triggerRef = useRef<View>(null);
  const [languagePickerOpen, setLanguagePickerOpen] = useState(false);
  const [anchor, setAnchor] = useState<AnchorRect | null>(null);

  const isDark = mode === 'dark';

  const separatorColor = useMemo(
    () => `${colors.textSecondary}26`,
    [colors.textSecondary]
  );

  const closeLanguagePicker = useCallback(() => {
    setLanguagePickerOpen(false);
    setAnchor(null);
  }, []);

  const openLanguagePicker = useCallback(() => {
    const winH = Dimensions.get('window').height;
    triggerRef.current?.measureInWindow((x, y, w, h) => {
      const spaceBelow = winH - (y + h);
      const openAbove =
        spaceBelow < LANGUAGE_PANEL_ESTIMATE && y > LANGUAGE_PANEL_ESTIMATE;
      setAnchor({ x, y, width: w, height: h, openAbove });
      setLanguagePickerOpen(true);
    });
  }, []);

  return (
    <View style={[styles.root, { backgroundColor: colors.background }]}>
      <View
        style={[
          styles.card,
          {
            backgroundColor: colors.menuBackground,
            borderColor: colors.textSecondary + '44',
          },
        ]}
      >
        <Text style={[styles.groupLabel, { color: colors.textSecondary }]}>
          {t('settings.themeSection')}
        </Text>
        <View style={styles.themeRow}>
          <Text style={[styles.rowPrimary, { color: colors.text }]}>
            {t('settings.darkTheme')}
          </Text>
          <Switch
            accessibilityLabel={t('settings.darkTheme')}
            value={isDark}
            onValueChange={(on) => setMode(on ? 'dark' : 'light')}
            trackColor={{
              false: colors.textSecondary + '55',
              true: colors.interactive,
            }}
            thumbColor={
              Platform.OS === 'android'
                ? isDark
                  ? colors.topBarText
                  : '#f4f3f4'
                : undefined
            }
            ios_backgroundColor={colors.textSecondary + '55'}
          />
        </View>

        <Text
          style={[
            styles.groupLabel,
            styles.groupLabelSpaced,
            { color: colors.textSecondary },
          ]}
        >
          {t('settings.languageSection')}
        </Text>
        <View ref={triggerRef} collapsable={false}>
          <Pressable
            accessibilityRole="button"
            accessibilityLabel={t('a11y.languagePicker')}
            accessibilityState={{ expanded: languagePickerOpen }}
            onPress={openLanguagePicker}
            style={({ pressed }) => [
              styles.dropdown,
              {
                borderColor: colors.textSecondary + '55',
                backgroundColor: colors.background,
                ...(languagePickerOpen && anchor
                  ? anchor.openAbove
                    ? {
                        borderTopLeftRadius: 0,
                        borderTopRightRadius: 0,
                      }
                    : {
                        borderBottomLeftRadius: 0,
                        borderBottomRightRadius: 0,
                      }
                  : null),
              },
              pressed && styles.dropdownPressed,
            ]}
          >
            <Text style={[styles.dropdownValue, { color: colors.text }]}>
              {localeLabel(locale, t)}
            </Text>
            <MaterialIcons
              name={languagePickerOpen ? 'keyboard-arrow-up' : 'keyboard-arrow-down'}
              size={24}
              color={colors.textSecondary}
            />
          </Pressable>
        </View>
      </View>

      <View style={[styles.versionBlock, { borderTopColor: colors.textSecondary + '33' }]}>
        <Text style={[styles.versionLabel, { color: colors.textSecondary }]}>
          {t('settings.versionLabel')}
        </Text>
        <Text style={[styles.versionValue, { color: colors.text }]}>{version}</Text>
      </View>

      <Modal
        visible={languagePickerOpen}
        transparent
        animationType="fade"
        onRequestClose={closeLanguagePicker}
      >
        <View style={styles.modalRoot}>
          <Pressable
            accessibilityRole="button"
            accessibilityLabel={t('a11y.dismissOverlay')}
            style={[styles.modalBackdrop, { backgroundColor: 'rgba(0,0,0,0.5)' }]}
            onPress={closeLanguagePicker}
          />
          {anchor ? (
            <View
              style={[
                styles.languagePanel,
                anchor.openAbove ? styles.languagePanelAbove : styles.languagePanelBelow,
                {
                  position: 'absolute',
                  left: anchor.x,
                  width: anchor.width,
                  top: anchor.openAbove
                    ? anchor.y - LANGUAGE_PANEL_ESTIMATE
                    : anchor.y + anchor.height,
                  backgroundColor: colors.background,
                  borderColor: colors.textSecondary + '55',
                },
              ]}
            >
              {APP_LOCALES.map((code, index) => {
                const selected = locale === code;
                return (
                  <View key={code}>
                    {index > 0 ? (
                      <View
                        style={[
                          styles.languageDivider,
                          { backgroundColor: separatorColor },
                        ]}
                      />
                    ) : null}
                    <Pressable
                      android_ripple={{ color: `${colors.interactive}55` }}
                      onPress={() => {
                        setLocale(code);
                        closeLanguagePicker();
                      }}
                      style={({ pressed }) => [
                        styles.languageOption,
                        selected && {
                          backgroundColor: `${colors.interactive}33`,
                        },
                        pressed && { backgroundColor: `${colors.interactive}33` },
                      ]}
                    >
                      <Text
                        style={[
                          styles.languageOptionText,
                          { color: colors.text },
                          selected && styles.languageOptionTextSelected,
                        ]}
                      >
                        {localeLabel(code, t)}
                      </Text>
                      <View style={styles.languageOptionTrail}>
                        {selected ? (
                          <MaterialIcons
                            name="check"
                            size={20}
                            color={colors.topBar}
                          />
                        ) : null}
                      </View>
                    </Pressable>
                  </View>
                );
              })}
            </View>
          ) : null}
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    paddingHorizontal: 24,
    paddingTop: 16,
  },
  card: {
    borderRadius: 14,
    borderWidth: StyleSheet.hairlineWidth,
    paddingHorizontal: 18,
    paddingTop: 16,
    paddingBottom: 18,
  },
  groupLabel: {
    fontSize: 13,
    fontWeight: '600',
    letterSpacing: 0.3,
    textTransform: 'uppercase',
    marginBottom: 10,
  },
  groupLabelSpaced: {
    marginTop: 20,
    marginBottom: 10,
  },
  themeRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    minHeight: 48,
    paddingVertical: 4,
  },
  rowPrimary: {
    fontSize: 17,
    flex: 1,
    marginRight: 12,
  },
  dropdown: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    minHeight: 48,
    paddingVertical: 10,
    paddingHorizontal: 14,
    borderRadius: 10,
    borderWidth: StyleSheet.hairlineWidth,
  },
  dropdownPressed: {
    opacity: 0.92,
  },
  dropdownValue: {
    fontSize: 16,
    fontWeight: '500',
  },
  versionBlock: {
    marginTop: 28,
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
  modalRoot: {
    flex: 1,
  },
  modalBackdrop: {
    ...StyleSheet.absoluteFillObject,
  },
  languagePanel: {
    borderWidth: StyleSheet.hairlineWidth,
    overflow: 'hidden',
  },
  languagePanelBelow: {
    borderTopWidth: 0,
    borderTopLeftRadius: 0,
    borderTopRightRadius: 0,
    borderBottomLeftRadius: 10,
    borderBottomRightRadius: 10,
  },
  languagePanelAbove: {
    borderBottomWidth: 0,
    borderBottomLeftRadius: 0,
    borderBottomRightRadius: 0,
    borderTopLeftRadius: 10,
    borderTopRightRadius: 10,
  },
  languageDivider: {
    height: StyleSheet.hairlineWidth,
    marginHorizontal: 14,
  },
  languageOption: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: 14,
    paddingHorizontal: 14,
    minHeight: 48,
  },
  languageOptionText: {
    fontSize: 16,
    fontWeight: '500',
    flex: 1,
  },
  languageOptionTextSelected: {
    fontWeight: '600',
  },
  languageOptionTrail: {
    width: 28,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
