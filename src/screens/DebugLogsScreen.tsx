import * as FileSystem from 'expo-file-system/legacy';
import * as Sharing from 'expo-sharing';
import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import {
  debugLogClear,
  debugLogFormatExport,
  debugLogGetSnapshot,
  debugLogSubscribe,
  type DebugLogEntry,
} from '../debug/DebugLog';
import { useI18n } from '../i18n';
import { useTheme } from '../theme';

export function DebugLogsScreen() {
  const { colors } = useTheme();
  const { t } = useI18n();
  const insets = useSafeAreaInsets();
  const [entries, setEntries] = useState<DebugLogEntry[]>(() => debugLogGetSnapshot());
  const [exporting, setExporting] = useState(false);

  useEffect(() => {
    return debugLogSubscribe(() => {
      setEntries(debugLogGetSnapshot());
    });
  }, []);

  const onClear = useCallback(() => {
    debugLogClear();
    setEntries([]);
  }, []);

  const onExport = useCallback(async () => {
    const cache = FileSystem.cacheDirectory;
    if (!cache) {
      Alert.alert(t('debugLogs.title'), t('debugLogs.exportNoCache'));
      return;
    }
    setExporting(true);
    try {
      const body = debugLogFormatExport();
      const name = `chitalka-logs-${new Date().toISOString().replace(/[:.]/g, '-')}.txt`;
      const uri = `${cache.endsWith('/') ? cache : `${cache}/`}${name}`;
      await FileSystem.writeAsStringAsync(uri, body, {
        encoding: FileSystem.EncodingType.UTF8,
      });
      const canShare = await Sharing.isAvailableAsync();
      if (!canShare) {
        Alert.alert(t('debugLogs.title'), `${t('debugLogs.exportSaved')}\n${uri}`);
        return;
      }
      await Sharing.shareAsync(uri, {
        mimeType: 'text/plain',
        dialogTitle: t('debugLogs.exportDialogTitle'),
      });
    } catch (e) {
      const msg = e instanceof Error ? e.message : String(e);
      Alert.alert(t('debugLogs.title'), `${t('debugLogs.exportFailed')}: ${msg}`);
    } finally {
      setExporting(false);
    }
  }, [t]);

  const renderItem = useCallback(
    ({ item }: { item: DebugLogEntry }) => (
      <Text style={[styles.line, { color: colors.text }]} selectable>
        <Text style={[styles.level, { color: colors.textSecondary }]}>{item.level}</Text>
        {'  '}
        {item.message}
      </Text>
    ),
    [colors.text, colors.textSecondary]
  );

  const keyExtractor = useCallback((item: DebugLogEntry, index: number) => `${item.ts}-${index}`, []);

  const listEmpty = useMemo(
    () => (
      <Text style={[styles.empty, { color: colors.textSecondary }]}>{t('debugLogs.empty')}</Text>
    ),
    [colors.textSecondary, t]
  );

  return (
    <View style={[styles.root, { backgroundColor: colors.background, paddingTop: insets.top }]}>
      <Text style={[styles.title, { color: colors.text }]}>{t('debugLogs.title')}</Text>
      <Text style={[styles.subtitle, { color: colors.textSecondary }]}>{t('debugLogs.subtitle')}</Text>

      <View style={styles.toolbar}>
        <Pressable
          onPress={onClear}
          disabled={exporting || entries.length === 0}
          style={({ pressed }) => [
            styles.btn,
            { backgroundColor: colors.interactive },
            (exporting || entries.length === 0) && styles.btnDisabled,
            pressed && !(exporting || entries.length === 0) && styles.btnPressed,
          ]}
        >
          <Text style={[styles.btnText, { color: colors.text }]}>{t('debugLogs.clear')}</Text>
        </Pressable>
        <Pressable
          onPress={() => void onExport()}
          disabled={exporting || entries.length === 0}
          style={({ pressed }) => [
            styles.btn,
            { backgroundColor: colors.interactive },
            (exporting || entries.length === 0) && styles.btnDisabled,
            pressed && !(exporting || entries.length === 0) && styles.btnPressed,
          ]}
        >
          {exporting ? (
            <ActivityIndicator color={colors.text} />
          ) : (
            <Text style={[styles.btnText, { color: colors.text }]}>{t('debugLogs.export')}</Text>
          )}
        </Pressable>
      </View>

      <FlatList
        data={entries}
        keyExtractor={keyExtractor}
        renderItem={renderItem}
        contentContainerStyle={styles.listContent}
        ListEmptyComponent={listEmpty}
        initialNumToRender={24}
        maxToRenderPerBatch={32}
        windowSize={10}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    paddingHorizontal: 16,
  },
  title: {
    fontSize: 22,
    fontWeight: '700',
    marginBottom: 6,
  },
  subtitle: {
    fontSize: 14,
    lineHeight: 20,
    marginBottom: 14,
  },
  toolbar: {
    flexDirection: 'row',
    gap: 10,
    marginBottom: 12,
  },
  btn: {
    flex: 1,
    paddingVertical: 12,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: 48,
  },
  btnPressed: {
    opacity: 0.88,
  },
  btnDisabled: {
    opacity: 0.45,
  },
  btnText: {
    fontSize: 16,
    fontWeight: '600',
  },
  listContent: {
    paddingBottom: 24,
    flexGrow: 1,
  },
  line: {
    fontSize: 12,
    fontFamily: 'monospace',
    marginBottom: 6,
    lineHeight: 16,
  },
  level: {
    fontWeight: '700',
  },
  empty: {
    marginTop: 32,
    textAlign: 'center',
    fontSize: 15,
  },
});
