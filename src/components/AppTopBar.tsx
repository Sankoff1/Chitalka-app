import MaterialIcons from '@expo/vector-icons/MaterialIcons';
import type { DrawerHeaderProps } from '@react-navigation/drawer';
import { Alert, Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { useLibrary } from '../context/LibraryContext';
import { useI18n } from '../i18n';
import { useTheme } from '../theme';

export function AppTopBar({ navigation, options }: DrawerHeaderProps) {
  const { colors } = useTheme();
  const { t } = useI18n();
  const { bookCount, openBooksForSearch } = useLibrary();

  const title =
    typeof options.title === 'string' && options.title.length > 0
      ? options.title
      : '';

  const showSearch = bookCount > 0;

  return (
    <SafeAreaView
      edges={['top', 'left', 'right']}
      style={[styles.safe, { backgroundColor: colors.topBar }]}
    >
      <View style={styles.row}>
        <View style={styles.sideSlot}>
          <Pressable
            accessibilityLabel={t('a11y.openMenu')}
            hitSlop={12}
            onPress={() => navigation.openDrawer()}
            style={({ pressed }) => [styles.iconBtn, pressed && styles.pressed]}
          >
            <MaterialIcons name="menu" size={26} color={colors.topBarText} />
          </Pressable>
        </View>
        <View style={styles.titleWrap}>
          <Text
            style={[styles.title, { color: colors.topBarText }]}
            numberOfLines={1}
          >
            {title}
          </Text>
        </View>
        <View style={[styles.sideSlot, styles.sideSlotRight]}>
          {showSearch ? (
            <Pressable
              accessibilityLabel={t('a11y.search')}
              hitSlop={12}
              onPress={openBooksForSearch}
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
  iconBtn: {
    padding: 8,
    justifyContent: 'center',
    alignItems: 'center',
  },
  pressed: {
    opacity: 0.82,
  },
});
