import MaterialIcons from '@expo/vector-icons/MaterialIcons';
import type { DrawerHeaderProps } from '@react-navigation/drawer';
import { useEffect, useRef } from 'react';
import {
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { useLibrary } from '../context/LibraryContext';
import { useI18n } from '../i18n';
import { useTheme } from '../theme';

/** Экраны drawer, у которых поиск по книгам не имеет смысла. */
const NON_SEARCHABLE_ROUTES = new Set(['Settings', 'DebugLogs']);

export function AppTopBar({ navigation, route, options }: DrawerHeaderProps) {
  const { colors } = useTheme();
  const { t } = useI18n();
  const {
    bookCount,
    isSearchOpen,
    searchQuery,
    openSearch,
    closeSearch,
    setSearchQuery,
  } = useLibrary();
  const inputRef = useRef<TextInput | null>(null);

  const title =
    typeof options.title === 'string' && options.title.length > 0
      ? options.title
      : '';

  const searchable = !NON_SEARCHABLE_ROUTES.has(route.name);
  const showSearchButton = searchable && bookCount > 0 && !isSearchOpen;
  const showSearchInput = searchable && isSearchOpen;

  /* Если пользователь уходит на экран без поиска, закрываем строку поиска,
     чтобы при возврате на список книг она не оставалась висеть с прошлым запросом. */
  useEffect(() => {
    if (!searchable && isSearchOpen) {
      closeSearch();
    }
  }, [closeSearch, isSearchOpen, searchable]);

  useEffect(() => {
    if (showSearchInput) {
      const id = setTimeout(() => inputRef.current?.focus(), 50);
      return () => clearTimeout(id);
    }
    return undefined;
  }, [showSearchInput]);

  return (
    <SafeAreaView
      edges={['top', 'left', 'right']}
      style={[styles.safe, { backgroundColor: colors.topBar }]}
    >
      <View style={styles.row}>
        <View style={styles.sideSlot}>
          {showSearchInput ? (
            <Pressable
              accessibilityLabel={t('a11y.closeSearch')}
              hitSlop={12}
              onPress={closeSearch}
              style={({ pressed }) => [
                styles.iconBtn,
                pressed && styles.pressed,
              ]}
            >
              <MaterialIcons
                name="arrow-back"
                size={26}
                color={colors.topBarText}
              />
            </Pressable>
          ) : (
            <Pressable
              accessibilityLabel={t('a11y.openMenu')}
              hitSlop={12}
              onPress={() => navigation.openDrawer()}
              style={({ pressed }) => [
                styles.iconBtn,
                pressed && styles.pressed,
              ]}
            >
              <MaterialIcons name="menu" size={26} color={colors.topBarText} />
            </Pressable>
          )}
        </View>
        <View style={styles.titleWrap}>
          {showSearchInput ? (
            <TextInput
              ref={inputRef}
              value={searchQuery}
              onChangeText={setSearchQuery}
              placeholder={t('search.placeholder')}
              placeholderTextColor={`${colors.topBarText}99`}
              style={[styles.input, { color: colors.topBarText }]}
              returnKeyType="search"
              autoCorrect={false}
              autoCapitalize="none"
              underlineColorAndroid="transparent"
              selectionColor={colors.topBarText}
            />
          ) : (
            <Text
              style={[styles.title, { color: colors.topBarText }]}
              numberOfLines={1}
            >
              {title}
            </Text>
          )}
        </View>
        <View style={[styles.sideSlot, styles.sideSlotRight]}>
          {showSearchInput && searchQuery.length > 0 ? (
            <Pressable
              accessibilityLabel={t('a11y.closeSearch')}
              hitSlop={12}
              onPress={() => setSearchQuery('')}
              style={({ pressed }) => [
                styles.iconBtn,
                pressed && styles.pressed,
              ]}
            >
              <MaterialIcons
                name="close"
                size={24}
                color={colors.topBarText}
              />
            </Pressable>
          ) : showSearchButton ? (
            <Pressable
              accessibilityLabel={t('a11y.search')}
              hitSlop={12}
              onPress={openSearch}
              style={({ pressed }) => [
                styles.iconBtn,
                pressed && styles.pressed,
              ]}
            >
              <MaterialIcons
                name="search"
                size={26}
                color={colors.topBarText}
              />
            </Pressable>
          ) : null}
        </View>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: {
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: 'rgba(0,0,0,0.12)',
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    minHeight: 53,
    paddingHorizontal: 4,
  },
  sideSlot: {
    width: 48,
    justifyContent: 'center',
    alignItems: 'flex-start',
  },
  sideSlotRight: {
    alignItems: 'flex-end',
  },
  titleWrap: {
    flex: 1,
    paddingHorizontal: 8,
    justifyContent: 'center',
  },
  title: {
    fontSize: 18,
    fontWeight: '700',
    textAlign: 'center',
  },
  input: {
    fontSize: 17,
    paddingVertical: 0,
    paddingHorizontal: 0,
  },
  iconBtn: {
    padding: 8,
    justifyContent: 'center',
    alignItems: 'center',
  },
  pressed: {
    opacity: 0.82,
  },
});
