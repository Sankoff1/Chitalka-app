import * as DocumentPicker from 'expo-document-picker';
import { useCallback, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';

export type LibraryScreenProps = {
  onBookSelected: (uri: string, bookId: string) => void;
};

function deriveBookId(fileName: string): string {
  const base = fileName.replace(/^.*[/\\]/, '').trim();
  const withoutExt = base.replace(/\.epub$/i, '').trim();
  return withoutExt.length > 0 ? withoutExt : `book_${Date.now()}`;
}

function isEpubFileName(name: string): boolean {
  return name.trim().toLowerCase().endsWith('.epub');
}

export function LibraryScreen({ onBookSelected }: LibraryScreenProps) {
  const [busy, setBusy] = useState(false);
  const [hint, setHint] = useState<string | null>(null);

  const pickEpub = useCallback(async () => {
    setHint(null);
    setBusy(true);
    try {
      const result = await DocumentPicker.getDocumentAsync({
        type: 'application/epub+zip',
        copyToCacheDirectory: true,
        multiple: false,
      });

      if (result.canceled || !result.assets?.length) {
        return;
      }

      const asset = result.assets[0];
      if (!isEpubFileName(asset.name)) {
        setHint('Выберите файл с расширением .epub.');
        return;
      }

      onBookSelected(asset.uri, deriveBookId(asset.name));
    } catch {
      setHint('Не удалось открыть выбор файла. Попробуйте ещё раз.');
    } finally {
      setBusy(false);
    }
  }, [onBookSelected]);

  return (
    <View style={styles.root}>
      <Text style={styles.title}>Библиотека</Text>
      <Text style={styles.subtitle}>
        Откройте книгу в формате EPUB с устройства.
      </Text>

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
          <Text style={styles.primaryButtonText}>Выбрать .epub</Text>
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
