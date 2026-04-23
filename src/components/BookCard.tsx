import { Image, Pressable, StyleSheet, Text, View } from 'react-native';

import { useI18n } from '../i18n';
import { useTheme } from '../theme';

export type BookCardProps = {
  title: string;
  author: string;
  /** Размер файла в мегабайтах (уже переведённый). */
  fileSizeMb: number;
  coverUri?: string | null;
  onPress: () => void;
};

export function BookCard({
  title,
  author,
  fileSizeMb,
  coverUri,
  onPress,
}: BookCardProps) {
  const { colors } = useTheme();
  const { t } = useI18n();

  return (
    <Pressable
      accessibilityRole="button"
      onPress={onPress}
      style={({ pressed }) => [
        styles.card,
        { backgroundColor: colors.interactive },
        pressed && styles.pressed,
      ]}
    >
      <View style={styles.row}>
        <View
          style={[
            styles.coverWrap,
            { backgroundColor: colors.menuBackground },
          ]}
        >
          {coverUri ? (
            <Image
              source={{ uri: coverUri }}
              style={styles.coverImage}
              resizeMode="cover"
            />
          ) : (
            <Text style={[styles.coverPlaceholder, { color: colors.textSecondary }]}>
              📖
            </Text>
          )}
        </View>
        <View style={styles.textBlock}>
          <Text
            numberOfLines={2}
            style={[styles.title, { color: colors.text }]}
          >
            {title}
          </Text>
          <Text
            numberOfLines={2}
            style={[styles.author, { color: colors.textSecondary }]}
          >
            {author}
          </Text>
          <Text style={[styles.size, { color: colors.textSecondary }]}>
            {fileSizeMb.toFixed(2)} {t('common.mb')}
          </Text>
        </View>
      </View>
    </Pressable>
  );
}

const COVER_W = 72;

const styles = StyleSheet.create({
  card: {
    borderRadius: 12,
    padding: 12,
    marginBottom: 12,
  },
  pressed: {
    opacity: 0.9,
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
});
