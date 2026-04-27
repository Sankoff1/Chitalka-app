import { useCallback, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import { useLibrary } from '../context/LibraryContext';
import { useI18n } from '../i18n';
import { pickEpubAsset } from '../utils/epubPicker';

export type LibraryScreenProps = {
  onBookSelected: (uri: string, bookId: string) => void | Promise<void>;
};

export function LibraryScreen({ onBookSelected }: LibraryScreenProps) {
  const { t } = useI18n();
  const { bumpLibraryEpoch, refreshBookCount } = useLibrary();
  const [busy, setBusy] = useState(false);
  const [hint, setHint] = useState<string | null>(null);

  const pickEpub = useCallback(async () => {
    setHint(null);
    setBusy(true);
    try {
      const result = await pickEpubAsset();
      if (result.kind === 'canceled') {
        return;
      }
      if (result.kind === 'error') {
        setHint(t(result.messageKey));
        return;
      }
      console.log('[Chitalka][Импорт]', 'Файл выбран', {
        bookId: result.bookId,
        uriPreview: result.uri.slice(0, 72),
      });
      await Promise.resolve(onBookSelected(result.uri, result.bookId));
      bumpLibraryEpoch();
      await refreshBookCount();
    } catch (e) {
      const msg = e instanceof Error ? e.message : String(e);
      Alert.alert('', msg);
    } finally {
      setBusy(false);
    }
  }, [bumpLibraryEpoch, onBookSelected, refreshBookCount, t]);

  return (
    <View style={styles.root}>
      <Text style={styles.title}>{t('libraryScreen.title')}</Text>
      <Text style={styles.subtitle}>{t('libraryScreen.subtitle')}</Text>

      <Pressable
        onPress={pickEpub}
        disabled={busy}
        style={({ pressed }) => [
          styles.primaryButton,
          busy && styles.primaryButtonDisabled,
          pressed && !busy && styles.primaryButtonPressed,
        ]}
      >
        {busy ? (
          <ActivityIndicator color="#222" />
        ) : (
          <Text style={styles.primaryButtonText}>{t('libraryScreen.pickEpub')}</Text>
        )}
      </Pressable>

      {hint ? <Text style={styles.hint}>{hint}</Text> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    paddingHorizontal: 24,
    paddingTop: 48,
    paddingBottom: 24,
    backgroundColor: '#f6f6f4',
    justifyContent: 'center',
  },
  title: {
    fontSize: 26,
    fontWeight: '700',
    color: '#111',
    marginBottom: 10,
  },
  subtitle: {
    fontSize: 16,
    color: '#555',
    lineHeight: 22,
    marginBottom: 28,
  },
  primaryButton: {
    alignSelf: 'flex-start',
    paddingVertical: 14,
    paddingHorizontal: 20,
    borderRadius: 10,
    backgroundColor: '#e8e6e1',
    minWidth: 180,
    alignItems: 'center',
    justifyContent: 'center',
  },
  primaryButtonPressed: {
    opacity: 0.88,
  },
  primaryButtonDisabled: {
    opacity: 0.55,
  },
  primaryButtonText: {
    fontSize: 16,
    color: '#222',
    fontWeight: '600',
  },
  hint: {
    marginTop: 16,
    fontSize: 15,
    color: '#a33',
    lineHeight: 21,
  },
});
