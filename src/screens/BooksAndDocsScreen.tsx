import MaterialIcons from '@expo/vector-icons/MaterialIcons';
import { useFocusEffect } from '@react-navigation/native';
import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { BookCard } from '../components/BookCard';
import { useLibrary } from '../context/LibraryContext';
import { useI18n } from '../i18n';
import type { LibraryBookRecord } from '../core/types';
import { StorageService } from '../database/StorageService';
import { navigateToReader } from '../navigation/navigationRef';
import { useTheme } from '../theme';

export function BooksAndDocsScreen() {
  const { colors } = useTheme();
  const insets = useSafeAreaInsets();
  const { t } = useI18n();
  const { pickEpubFromToolbar, libraryEpoch } = useLibrary();
  const storage = useMemo(() => new StorageService(), []);
  const [books, setBooks] = useState<LibraryBookRecord[]>([]);
  const [loading, setLoading] = useState(true);

  const loadBooks = useCallback(async () => {
    setLoading(true);
    try {
      const list = await storage.listLibraryBooks();
      setBooks(list);
    } catch {
      setBooks([]);
    } finally {
      setLoading(false);
    }
  }, [storage]);

  useFocusEffect(
    useCallback(() => {
      void loadBooks();
    }, [loadBooks])
  );

  useEffect(() => {
    if (libraryEpoch > 0) {
      void loadBooks();
    }
  }, [libraryEpoch, loadBooks]);

  const openReader = useCallback((item: LibraryBookRecord) => {
    navigateToReader(item.fileUri, item.bookId);
  }, []);

  const fabBottom = insets.bottom + 16;
  const listPaddingBottom = fabBottom + 56 + 16;

  const renderItem = useCallback(
    ({ item }: { item: LibraryBookRecord }) => (
      <BookCard
        title={item.title}
        author={item.author}
        fileSizeMb={item.fileSizeBytes / (1024 * 1024)}
        coverUri={item.coverUri}
        onPress={() => openReader(item)}
      />
    ),
    [openReader]
  );

  return (
    <View style={[styles.root, { backgroundColor: colors.background }]}>
      {loading ? (
        <View style={styles.centered}>
          <ActivityIndicator size="large" color={colors.topBar} />
        </View>
      ) : (
        <FlatList
          data={books}
          keyExtractor={(item) => item.bookId}
          renderItem={renderItem}
          contentContainerStyle={[
            styles.listContent,
            { paddingBottom: listPaddingBottom },
          ]}
          ListEmptyComponent={
            <Text style={[styles.empty, { color: colors.textSecondary }]}>
              {t('books.empty')}
            </Text>
          }
        />
      )}
      <Pressable
        accessibilityLabel={t('books.addBookA11y')}
        onPress={() => {
          void pickEpubFromToolbar();
        }}
        style={({ pressed }) => [
          styles.fab,
          { bottom: fabBottom, backgroundColor: colors.topBar },
          pressed && styles.fabPressed,
        ]}
      >
        <MaterialIcons name="add" size={30} color={colors.topBarText} />
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
  },
  listContent: {
    padding: 16,
  },
  centered: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  empty: {
    textAlign: 'center',
    marginTop: 48,
    paddingHorizontal: 24,
    fontSize: 16,
    lineHeight: 22,
  },
  fab: {
    position: 'absolute',
    right: 20,
    width: 56,
    height: 56,
    borderRadius: 28,
    alignItems: 'center',
    justifyContent: 'center',
    elevation: 4,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3,
  },
  fabPressed: {
    opacity: 0.9,
  },
});
