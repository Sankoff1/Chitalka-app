import {
  createDrawerNavigator,
  type DrawerHeaderProps,
} from '@react-navigation/drawer';
import { useMemo } from 'react';
import { useWindowDimensions } from 'react-native';

import { AppTopBar } from '../components/AppTopBar';
import { PlaceholderScreen } from '../screens/PlaceholderScreen';
import { BooksAndDocsScreen } from '../screens/BooksAndDocsScreen';
import { DebugLogsScreen } from '../screens/DebugLogsScreen';
import { SettingsScreen } from '../screens/SettingsScreen';
import { useI18n } from '../i18n';
import { useTheme } from '../theme';
import type { DrawerParamList } from './types';

function ReadingNowScreen() {
  const { t } = useI18n();
  return (
    <PlaceholderScreen
      title={t('screens.readingNow.title')}
      subtitle={t('screens.readingNow.subtitle')}
    />
  );
}

function FavoritesScreen() {
  const { t } = useI18n();
  return (
    <PlaceholderScreen
      title={t('screens.favorites.title')}
      subtitle={t('screens.favorites.subtitle')}
    />
  );
}

function AuthorsScreen() {
  const { t } = useI18n();
  return (
    <PlaceholderScreen
      title={t('screens.authors.title')}
      subtitle={t('screens.authors.subtitle')}
    />
  );
}

function CollectionsScreen() {
  const { t } = useI18n();
  return (
    <PlaceholderScreen
      title={t('screens.collections.title')}
      subtitle={t('screens.collections.subtitle')}
    />
  );
}

function CartScreen() {
  const { t } = useI18n();
  return (
    <PlaceholderScreen
      title={t('screens.cart.title')}
      subtitle={t('screens.cart.subtitle')}
    />
  );
}

const Drawer = createDrawerNavigator<DrawerParamList>();

const DRAWER_TARGET_WIDTH = 288;

export function AppDrawer() {
  const { colors } = useTheme();
  const { t } = useI18n();
  const { width: windowWidth } = useWindowDimensions();

  const drawerWidth = useMemo(
    () => Math.min(DRAWER_TARGET_WIDTH, windowWidth - 24),
    [windowWidth]
  );

  const readingOpts = useMemo(
    () => ({
      title: t('drawer.readingNow'),
      drawerLabel: t('drawer.readingNow'),
    }),
    [t]
  );
  const booksOpts = useMemo(
    () => ({
      title: t('drawer.books'),
      drawerLabel: t('drawer.books'),
    }),
    [t]
  );
  const favOpts = useMemo(
    () => ({
      title: t('drawer.favorites'),
      drawerLabel: t('drawer.favorites'),
    }),
    [t]
  );
  const authorsOpts = useMemo(
    () => ({
      title: t('drawer.authors'),
      drawerLabel: t('drawer.authors'),
    }),
    [t]
  );
  const collOpts = useMemo(
    () => ({
      title: t('drawer.collections'),
      drawerLabel: t('drawer.collections'),
    }),
    [t]
  );
  const cartOpts = useMemo(
    () => ({
      title: t('drawer.cart'),
      drawerLabel: t('drawer.cart'),
    }),
    [t]
  );
  const debugLogsOpts = useMemo(
    () => ({
      title: t('drawer.debugLogs'),
      drawerLabel: t('drawer.debugLogs'),
    }),
    [t]
  );
  const settingsOpts = useMemo(
    () => ({
      title: t('drawer.settings'),
      drawerLabel: t('drawer.settings'),
    }),
    [t]
  );

  const screenOptions = useMemo(
    () => ({
      header: (props: DrawerHeaderProps) => <AppTopBar {...props} />,
      drawerStyle: {
        backgroundColor: colors.menuBackground,
        width: drawerWidth,
      },
      drawerActiveTintColor: colors.topBar,
      drawerInactiveTintColor: colors.textSecondary,
      drawerActiveBackgroundColor: `${colors.interactive}55`,
    }),
    [colors, drawerWidth]
  );

  return (
    <Drawer.Navigator screenOptions={screenOptions}>
      <Drawer.Screen
        name="ReadingNow"
        component={ReadingNowScreen}
        options={readingOpts}
      />
      <Drawer.Screen
        name="BooksAndDocs"
        component={BooksAndDocsScreen}
        options={booksOpts}
      />
      <Drawer.Screen
        name="Favorites"
        component={FavoritesScreen}
        options={favOpts}
      />
      <Drawer.Screen
        name="Authors"
        component={AuthorsScreen}
        options={authorsOpts}
      />
      <Drawer.Screen
        name="Collections"
        component={CollectionsScreen}
        options={collOpts}
      />
      <Drawer.Screen name="Cart" component={CartScreen} options={cartOpts} />
      <Drawer.Screen
        name="DebugLogs"
        component={DebugLogsScreen}
        options={debugLogsOpts}
      />
      <Drawer.Screen
        name="Settings"
        component={SettingsScreen}
        options={settingsOpts}
      />
    </Drawer.Navigator>
  );
}
