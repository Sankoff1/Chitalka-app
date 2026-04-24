import { useFocusEffect } from '@react-navigation/native';
import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  FlatList,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { BookActionsSheet } from '../components/BookActionsSheet';
import { BookCard } from '../components/BookCard';
import { useLibrary } from '../context/LibraryContext';
import type { LibraryBookWithProgress } from '../core/types';
import { StorageService } from '../database/StorageService';
import { useI18n } from '../i18n';
import { navigateToReader } from '../navigation/navigationRef';
import { useTheme } from '../theme';

export function FavoritesScreen() {
  const { colors } = useTheme();
  const insets = useSafeAreaInsets();
  const { t } = useI18n();
  const { libraryEpoch, bumpLibraryEpoch, refreshBookCount } = useLibrary();
  const storage = useMemo(() => new StorageService(), []);
  const [books, setBooks] = useState<LibraryBookWithProgress[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeBook, setActiveBook] =
    useState<LibraryBookWithProgress | null>(null);

  const loadBooks = useCallback(async () => {
    setLoading(true);
    try {
      const list = await storage.listFavoriteBooks();
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

  const openReader = useCallback((item: LibraryBookWithProgress) => {
    navigateToReader(item.fileUri, item.bookId);
  }, []);

  const removeFavorite = useCallback(
    (item: LibraryBookWithProgress) => {
      void (async () => {
        try {
          await storage.setBookFavorite(item.bookId, false);
        } finally {
          bumpLibraryEpoch();
        }
      })();
    },
    [bumpLibraryEpoch, storage]
  );

  const moveToTrash = useCallback(
    (item: LibraryBookWithProgress) => {
      void (async () => {
        try {
          await storage.moveBookToTrash(item.bookId);
        } finally {
          bumpLibraryEpoch();
          await refreshBookCount();
        }
      })();
    },
    [bumpLibraryEpoch, refreshBookCount, storage]
  );

  const sheetActions = useMemo(() => {
    if (!activeBook) {
      return [];
    }
    return [
      {
        key: 'unfavorite',
        icon: 'favorite' as const,
        label: t('bookActions.removeFromFavorites'),
        onPress: () => removeFavorite(activeBook),
      },
      {
        key: 'trash',
        icon: 'delete-outline' as const,
        label: t('bookActions.moveToTrash'),
        destructive: true,
        onPress: () => moveToTrash(activeBook),
      },
    ];
  }, [activeBook, moveToTrash, removeFavorite, t]);

  const renderItem = useCallback(
    ({ item }: { item: LibraryBookWithProgress }) => (
      <BookCard
        title={item.title}
        author={item.author}
        coverUri={item.coverUri}
        progress={item.progressFraction}
        isFavorite={item.isFavorite}
        onPress={() => openReader(item)}
        onLongPress={() => setActiveBook(item)}
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
            { paddingBottom: insets.bottom + 16 },
          ]}
          ListEmptyComponent={
            <Text style={[styles.empty, { color: colors.textSecondary }]}>
              {t('screens.favorites.empty')}
            </Text>
          }
        />
      )}
      <BookActionsSheet
        visible={activeBook !== null}
        title={activeBook?.title ?? ''}
        author={activeBook?.author ?? ''}
        coverUri={activeBook?.coverUri ?? null}
        actions={sheetActions}
        onClose={() => setActiveBook(null)}
      />
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
});
