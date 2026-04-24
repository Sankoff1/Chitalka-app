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

import { BookActionsSheet } from '../components/BookActionsSheet';
import { BookCard } from '../components/BookCard';
import { useLibrary } from '../context/LibraryContext';
import type { LibraryBookWithProgress } from '../core/types';
import { StorageService } from '../database/StorageService';
import { useI18n } from '../i18n';
import { navigateToReader } from '../navigation/navigationRef';
import { useTheme } from '../theme';

export function ReadingNowScreen() {
  const { colors } = useTheme();
  const insets = useSafeAreaInsets();
  const { t } = useI18n();
  const {
    pickEpubFromToolbar,
    libraryEpoch,
    bumpLibraryEpoch,
    refreshBookCount,
    searchQuery,
  } = useLibrary();
  const storage = useMemo(() => new StorageService(), []);
  const [books, setBooks] = useState<LibraryBookWithProgress[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeBook, setActiveBook] =
    useState<LibraryBookWithProgress | null>(null);

  const loadBooks = useCallback(async () => {
    setLoading(true);
    try {
      const list = await storage.listRecentlyReadBooks();
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

  const toggleFavorite = useCallback(
    (item: LibraryBookWithProgress) => {
      void (async () => {
        try {
          await storage.setBookFavorite(item.bookId, !item.isFavorite);
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
        key: 'favorite',
        icon: (activeBook.isFavorite ? 'favorite' : 'favorite-border') as
          | 'favorite'
          | 'favorite-border',
        label: activeBook.isFavorite
          ? t('bookActions.removeFromFavorites')
          : t('bookActions.addToFavorites'),
        onPress: () => toggleFavorite(activeBook),
      },
      {
        key: 'trash',
        icon: 'delete-outline' as const,
        label: t('bookActions.moveToTrash'),
        destructive: true,
        onPress: () => moveToTrash(activeBook),
      },
    ];
  }, [activeBook, moveToTrash, t, toggleFavorite]);

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
        onMenuPress={() => setActiveBook(item)}
      />
    ),
    [openReader]
  );

  const normalizedQuery = searchQuery.trim().toLocaleLowerCase();
  const visibleBooks = useMemo(() => {
    if (!normalizedQuery) {
      return books;
    }
    return books.filter(
      (b) =>
        b.title.toLocaleLowerCase().includes(normalizedQuery) ||
        b.author.toLocaleLowerCase().includes(normalizedQuery)
    );
  }, [books, normalizedQuery]);

  const fabBottom = insets.bottom + 16;
  const listPaddingBottom = fabBottom + 56 + 16;

  return (
    <View style={[styles.root, { backgroundColor: colors.background }]}>
      {loading ? (
        <View style={styles.centered}>
          <ActivityIndicator size="large" color={colors.topBar} />
        </View>
      ) : (
        <FlatList
          data={visibleBooks}
          keyExtractor={(item) => item.bookId}
          renderItem={renderItem}
          contentContainerStyle={[
            styles.listContent,
            { paddingBottom: listPaddingBottom },
          ]}
          ListEmptyComponent={
            <Text style={[styles.empty, { color: colors.textSecondary }]}>
              {normalizedQuery
                ? t('search.noResults')
                : t('screens.readingNow.subtitle')}
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
