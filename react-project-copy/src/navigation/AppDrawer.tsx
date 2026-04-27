import {
  createDrawerNavigator,
  type DrawerHeaderProps,
} from '@react-navigation/drawer';
import { useMemo } from 'react';
import { useWindowDimensions } from 'react-native';

import { AppTopBar } from '../components/AppTopBar';
import { BooksAndDocsScreen } from '../screens/BooksAndDocsScreen';
import { DebugLogsScreen } from '../screens/DebugLogsScreen';
import { FavoritesScreen } from '../screens/FavoritesScreen';
import { ReadingNowScreen } from '../screens/ReadingNowScreen';
import { SettingsScreen } from '../screens/SettingsScreen';
import { TrashScreen } from '../screens/TrashScreen';
import { useI18n } from '../i18n';
import { useTheme } from '../theme';
import type { DrawerParamList } from './types';

const Drawer = createDrawerNavigator<DrawerParamList>();

const DRAWER_TARGET_WIDTH = 288;

export function AppDrawer() {
  const { colors, mode } = useTheme();
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
      /* В тёмной теме topBar слишком тёмный для подписи на фоне меню — оставляем контраст как у текста. */
      drawerActiveTintColor: mode === 'dark' ? colors.topBarText : colors.topBar,
      drawerInactiveTintColor: mode === 'dark' ? colors.text : colors.textSecondary,
      drawerActiveBackgroundColor: `${colors.interactive}55`,
    }),
    [colors, drawerWidth, mode]
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
      <Drawer.Screen name="Cart" component={TrashScreen} options={cartOpts} />
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
