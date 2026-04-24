import * as FileSystem from 'expo-file-system/legacy';
import { useFocusEffect } from '@react-navigation/native';
import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  FlatList,
  Image,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { useLibrary } from '../context/LibraryContext';
import type { LibraryBookWithProgress } from '../core/types';
import { StorageService } from '../database/StorageService';
import { useI18n } from '../i18n';
import { useTheme } from '../theme';

export function TrashScreen() {
  const { colors } = useTheme();
  const insets = useSafeAreaInsets();
  const { t } = useI18n();
  const { libraryEpoch, bumpLibraryEpoch, refreshBookCount, searchQuery } =
    useLibrary();
  const storage = useMemo(() => new StorageService(), []);
  const [books, setBooks] = useState<LibraryBookWithProgress[]>([]);
  const [loading, setLoading] = useState(true);

  const loadBooks = useCallback(async () => {
    setLoading(true);
    try {
      const list = await storage.listTrashedBooks();
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

  const restore = useCallback(
    (item: LibraryBookWithProgress) => {
      void (async () => {
        try {
          await storage.restoreBookFromTrash(item.bookId);
        } finally {
          bumpLibraryEpoch();
          await refreshBookCount();
        }
      })();
    },
    [bumpLibraryEpoch, refreshBookCount, storage]
  );

  const purge = useCallback(
    (item: LibraryBookWithProgress) => {
      Alert.alert(
        t('trash.confirmDeleteTitle'),
        t('trash.confirmDeleteMessage'),
        [
          { text: t('common.cancel'), style: 'cancel' },
          {
            text: t('trash.deleteForever'),
            style: 'destructive',
            onPress: () => {
              void (async () => {
                try {
                  await storage.purgeBook(item.bookId);
                  if (item.fileUri) {
                    try {
                      await FileSystem.deleteAsync(item.fileUri, {
                        idempotent: true,
                      });
                    } catch {
                      /* ignore — файл мог уже исчезнуть */
                    }
                  }
                  if (item.coverUri) {
                    try {
                      await FileSystem.deleteAsync(item.coverUri, {
                        idempotent: true,
                      });
                    } catch {
                      /* ignore */
                    }
                  }
                } catch (e) {
                  const msg =
                    e instanceof Error
                      ? e.message || t('trash.deleteFailed')
                      : t('trash.deleteFailed');
                  Alert.alert('', msg);
                } finally {
                  bumpLibraryEpoch();
                  await refreshBookCount();
                }
              })();
            },
          },
        ]
      );
    },
    [bumpLibraryEpoch, refreshBookCount, storage, t]
  );

  const renderItem = useCallback(
    ({ item }: { item: LibraryBookWithProgress }) => (
      <View
        style={[styles.card, { backgroundColor: colors.interactive }]}
      >
        <View style={styles.row}>
          <View
            style={[
              styles.coverWrap,
              { backgroundColor: colors.menuBackground },
            ]}
          >
            {item.coverUri ? (
              <Image
                source={{ uri: item.coverUri }}
                style={styles.coverImage}
                resizeMode="cover"
              />
            ) : (
              <Text
                style={[
                  styles.coverPlaceholder,
                  { color: colors.textSecondary },
                ]}
              >
                📖
              </Text>
            )}
          </View>
          <View style={styles.textBlock}>
            <Text
              numberOfLines={2}
              style={[styles.title, { color: colors.text }]}
            >
              {item.title}
            </Text>
            <Text
              numberOfLines={2}
              style={[styles.author, { color: colors.textSecondary }]}
            >
              {item.author}
            </Text>
            <Text style={[styles.size, { color: colors.textSecondary }]}>
              {(item.fileSizeBytes / (1024 * 1024)).toFixed(2)} {t('common.mb')}
            </Text>
          </View>
        </View>
        <View style={styles.actions}>
          <Pressable
            onPress={() => restore(item)}
            style={({ pressed }) => [
              styles.actionButton,
              { backgroundColor: colors.topBar },
              pressed && styles.pressed,
            ]}
          >
            <Text style={[styles.actionLabel, { color: colors.topBarText }]}>
              {t('trash.restore')}
            </Text>
          </Pressable>
          <Pressable
            onPress={() => purge(item)}
            style={({ pressed }) => [
              styles.actionButton,
              styles.destructive,
              pressed && styles.pressed,
            ]}
          >
            <Text style={[styles.actionLabel, { color: '#FFFFFF' }]}>
              {t('trash.deleteForever')}
            </Text>
          </Pressable>
        </View>
      </View>
    ),
    [colors, purge, restore, t]
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
            { paddingBottom: insets.bottom + 16 },
          ]}
          ListEmptyComponent={
            <Text style={[styles.empty, { color: colors.textSecondary }]}>
              {normalizedQuery
                ? t('search.noResults')
                : t('screens.cart.empty')}
            </Text>
          }
        />
      )}
    </View>
  );
}

const COVER_W = 72;

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
  card: {
    borderRadius: 12,
    padding: 12,
    marginBottom: 12,
  },
  row: {
    flexDirection: 'row',
    gap: 12,
  },
  coverWrap: {
    width: COVER_W,
    height: COVER_W * 1.45,
    borderRadius: 8,
    overflow: 'hidden',
    alignItems: 'center',
    justifyContent: 'center',
  },
  coverImage: {
    width: '100%',
    height: '100%',
  },
  coverPlaceholder: {
    fontSize: 28,
  },
  textBlock: {
    flex: 1,
    justifyContent: 'center',
    minHeight: COVER_W * 1.45,
    gap: 4,
  },
  title: {
    fontSize: 17,
    fontWeight: '700',
    lineHeight: 22,
  },
  author: {
    fontSize: 14,
    lineHeight: 19,
  },
  size: {
    fontSize: 12,
    marginTop: 2,
  },
  actions: {
    flexDirection: 'row',
    gap: 8,
    marginTop: 12,
  },
  actionButton: {
    flex: 1,
    paddingVertical: 10,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  destructive: {
    backgroundColor: '#B3261E',
  },
  actionLabel: {
    fontSize: 14,
    fontWeight: '600',
  },
  pressed: {
    opacity: 0.85,
  },
});
