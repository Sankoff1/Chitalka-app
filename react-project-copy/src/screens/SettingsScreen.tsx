import MaterialIcons from '@expo/vector-icons/MaterialIcons';
import Constants from 'expo-constants';
import { Fragment, useCallback, useRef, useState } from 'react';
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
import Animated, { FadeInUp } from 'react-native-reanimated';

import { APP_LOCALES, useI18n, type AppLocale } from '../i18n';
import { useTheme } from '../theme';

/** Две строки по 48px + разделитель. */
const LANGUAGE_MENU_ESTIMATE = 97;

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

  const borderDropdown = colors.textSecondary + '2a';

  const closeLanguagePicker = useCallback(() => {
    setLanguagePickerOpen(false);
    setAnchor(null);
  }, []);

  const openLanguagePicker = useCallback(() => {
    const winH = Dimensions.get('window').height;
    triggerRef.current?.measureInWindow((x, y, w, h) => {
      const spaceBelow = winH - (y + h);
      const openAbove =
        spaceBelow < LANGUAGE_MENU_ESTIMATE && y > LANGUAGE_MENU_ESTIMATE;
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
                backgroundColor: colors.background,
                borderColor: borderDropdown,
                ...(languagePickerOpen && anchor
                  ? anchor.openAbove
                    ? {
                        borderTopLeftRadius: 0,
                        borderTopRightRadius: 0,
                        borderTopWidth: 0,
                      }
                    : {
                        borderBottomLeftRadius: 0,
                        borderBottomRightRadius: 0,
                        borderBottomWidth: 0,
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
              name={languagePickerOpen ? 'expand-less' : 'expand-more'}
              size={22}
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
        animationType="none"
        onRequestClose={closeLanguagePicker}
      >
        <View style={styles.modalRoot}>
          <Pressable
            accessibilityRole="button"
            accessibilityLabel={t('a11y.dismissOverlay')}
            style={styles.modalBackdrop}
            onPress={closeLanguagePicker}
          />
          {anchor ? (
            <Animated.View
              key={`${anchor.x}-${anchor.y}-${anchor.openAbove}`}
              entering={FadeInUp.duration(165)}
              style={[
                styles.languageMenu,
                anchor.openAbove ? styles.languageMenuAbove : styles.languageMenuBelow,
                {
                  position: 'absolute',
                  left: Math.round(anchor.x),
                  width: Math.round(anchor.x + anchor.width) - Math.round(anchor.x),
                  top: anchor.openAbove
                    ? Math.round(anchor.y) - LANGUAGE_MENU_ESTIMATE + 1
                    : Math.round(anchor.y + anchor.height) - 1,
                  backgroundColor: colors.background,
                  borderColor: borderDropdown,
                  zIndex: 1,
                },
              ]}
            >
              {APP_LOCALES.map((code, index) => {
                const selected = locale === code;
                return (
                  <Fragment key={code}>
                    {index > 0 ? (
                      <View
                        style={[
                          styles.languageDivider,
                          { backgroundColor: `${colors.textSecondary}26` },
                        ]}
                      />
                    ) : null}
                    <Pressable
                      android_ripple={{ color: `${colors.interactive}66` }}
                      onPress={() => {
                        setLocale(code);
                        closeLanguagePicker();
                      }}
                      style={({ pressed }) => [
                        styles.languageRow,
                        selected && { backgroundColor: `${colors.interactive}44` },
                        pressed && !selected && { backgroundColor: `${colors.interactive}22` },
                      ]}
                    >
                      <View style={styles.languageRowLabelWrap}>
                        <Text
                          style={[
                            styles.languageRowText,
                            { color: selected ? colors.topBar : colors.text },
                          ]}
                        >
                          {localeLabel(code, t)}
                        </Text>
                      </View>
                      <View style={styles.languageRowTrail}>
                        <MaterialIcons
                          name="check"
                          size={22}
                          color={colors.topBar}
                          style={!selected ? styles.languageCheckHidden : undefined}
                        />
                      </View>
                    </Pressable>
                  </Fragment>
                );
              })}
            </Animated.View>
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
    height: 48,
    paddingHorizontal: 16,
    borderRadius: 16,
    borderWidth: 1,
  },
  dropdownPressed: {
    opacity: 0.94,
  },
  dropdownValue: {
    fontSize: 16,
    fontWeight: '600',
    letterSpacing: 0.2,
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
    backgroundColor: 'transparent',
  },
  languageMenu: {
    borderWidth: 1,
    overflow: 'hidden',
  },
  languageMenuBelow: {
    borderTopWidth: 0,
    borderTopLeftRadius: 0,
    borderTopRightRadius: 0,
    borderBottomLeftRadius: 16,
    borderBottomRightRadius: 16,
  },
  languageMenuAbove: {
    borderBottomWidth: 0,
    borderBottomLeftRadius: 0,
    borderBottomRightRadius: 0,
    borderTopLeftRadius: 16,
    borderTopRightRadius: 16,
  },
  languageDivider: {
    height: StyleSheet.hairlineWidth,
    alignSelf: 'stretch',
  },
  languageRow: {
    flexDirection: 'row',
    alignItems: 'stretch',
    height: 48,
    paddingHorizontal: 16,
    overflow: 'hidden',
  },
  languageRowLabelWrap: {
    flex: 1,
    justifyContent: 'center',
    paddingRight: 8,
  },
  languageRowText: {
    fontSize: 16,
    fontWeight: '600',
    letterSpacing: 0.15,
    ...Platform.select({
      android: {
        includeFontPadding: false,
        textAlignVertical: 'center',
      },
      default: {},
    }),
  },
  languageRowTrail: {
    width: 28,
    alignItems: 'center',
    justifyContent: 'center',
  },
  languageCheckHidden: {
    opacity: 0,
  },
});
