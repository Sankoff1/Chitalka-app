import { NavigationContainer } from '@react-navigation/native';
import * as NavigationBar from 'expo-navigation-bar';
import { StatusBar } from 'expo-status-bar';
import { useEffect } from 'react';
import { Platform, View } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';

import { LibraryProvider } from './src/context/LibraryContext';
import {
  flushReaderNavigationIfPending,
  navigationRef,
} from './src/navigation/navigationRef';
import { RootStack } from './src/navigation/RootStack';
import { I18nProvider } from './src/i18n';
import { ThemeProvider, useTheme } from './src/theme';

function AndroidNavigationBar() {
  const { mode } = useTheme();

  useEffect(() => {
    if (Platform.OS !== 'android') {
      return;
    }
    void (async () => {
      try {
        await NavigationBar.setBackgroundColorAsync('#00000000');
        await NavigationBar.setBehaviorAsync('overlay-swipe');
      } catch {
        // Best-effort: older devices or config may reject some calls.
      }
    })();
  }, []);

  useEffect(() => {
    if (Platform.OS !== 'android') {
      return;
    }
    let cancelled = false;
    const frame = requestAnimationFrame(() => {
      void (async () => {
        if (cancelled) {
          return;
        }
        try {
          await NavigationBar.setButtonStyleAsync(
            mode === 'dark' ? 'light' : 'dark'
          );
        } catch {
          // ignore
        }
      })();
    });
    return () => {
      cancelled = true;
      cancelAnimationFrame(frame);
    };
  }, [mode]);

  return null;
}

function RootNavigator() {
  const { mode, colors } = useTheme();

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <AndroidNavigationBar />
      <StatusBar style={mode === 'dark' ? 'light' : 'dark'} />
      <NavigationContainer ref={navigationRef} onReady={flushReaderNavigationIfPending}>
        <LibraryProvider>
          <RootStack />
        </LibraryProvider>
      </NavigationContainer>
    </View>
  );
}

export default function App() {
  return (
    <SafeAreaProvider>
      <ThemeProvider>
        <I18nProvider>
          <RootNavigator />
        </I18nProvider>
      </ThemeProvider>
    </SafeAreaProvider>
  );
}
