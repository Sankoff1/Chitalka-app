import { Image, Pressable, StyleSheet, Text, View } from 'react-native';

import { useI18n } from '../i18n';
import { useTheme } from '../theme';

export type BookCardProps = {
  title: string;
  author: string;
  coverUri?: string | null;
  /** Прогресс чтения 0..1. `null`/`undefined` — полоса не показывается. */
  progress?: number | null;
  /** Если `true`, в правом верхнем углу обложки показывается отметка «Избранное». */
  isFavorite?: boolean;
  onPress: () => void;
  onLongPress?: () => void;
};

function clampFraction(value: number): number {
  if (!Number.isFinite(value)) {
    return 0;
  }
  return Math.min(1, Math.max(0, value));
}

export function BookCard({
  title,
  author,
  coverUri,
  progress,
  isFavorite,
  onPress,
  onLongPress,
}: BookCardProps) {
  const { colors } = useTheme();
  const { t } = useI18n();

  const hasProgress = typeof progress === 'number';
  const fraction = hasProgress ? clampFraction(progress as number) : 0;
  const percent = Math.round(fraction * 100);

  return (
    <Pressable
      accessibilityRole="button"
      onPress={onPress}
      onLongPress={onLongPress}
      delayLongPress={350}
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
            {
              backgroundColor: colors.menuBackground,
              borderColor: `${colors.textSecondary}33`,
            },
          ]}
        >
          {coverUri ? (
            <Image
              source={{ uri: coverUri }}
              style={styles.coverImage}
              resizeMode="cover"
            />
          ) : (
            <View style={styles.coverFallback}>
              <View
                style={[
                  styles.coverAccent,
                  { backgroundColor: colors.topBar },
                ]}
              />
              <Text
                numberOfLines={4}
                adjustsFontSizeToFit
                minimumFontScale={0.6}
                style={[styles.coverTitle, { color: colors.text }]}
              >
                {title}
              </Text>
              <View
                style={[
                  styles.coverRule,
                  { backgroundColor: `${colors.textSecondary}55` },
                ]}
              />
              <Text
                numberOfLines={2}
                style={[styles.coverAuthor, { color: colors.textSecondary }]}
              >
                {author}
              </Text>
            </View>
          )}
          {isFavorite ? (
            <View style={styles.favoriteBadge} accessibilityLabel="favorite">
              <Text style={styles.favoriteGlyph}>♥</Text>
            </View>
          ) : null}
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
          {hasProgress ? (
            <View
              style={styles.progressRow}
              accessibilityRole="progressbar"
              accessibilityValue={{ min: 0, max: 100, now: percent }}
            >
              <View
                style={[
                  styles.progressTrack,
                  { backgroundColor: colors.menuBackground },
                ]}
              >
                <View
                  style={[
                    styles.progressFill,
                    {
                      width: `${percent}%`,
                      backgroundColor: colors.topBar,
                    },
                  ]}
                />
              </View>
              <Text
                style={[styles.progressLabel, { color: colors.text }]}
              >
                {t('books.readPercent', { percent })}
              </Text>
            </View>
          ) : null}
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
    position: 'relative',
    borderWidth: StyleSheet.hairlineWidth,
  },
  coverFallback: {
    flex: 1,
    alignSelf: 'stretch',
    paddingHorizontal: 6,
    paddingVertical: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  coverAccent: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    height: 4,
  },
  coverTitle: {
    fontSize: 11,
    lineHeight: 13,
    fontWeight: '700',
    textAlign: 'center',
  },
  coverRule: {
    width: 28,
    height: 1,
    marginVertical: 6,
  },
  coverAuthor: {
    fontSize: 9,
    lineHeight: 11,
    textAlign: 'center',
    fontStyle: 'italic',
  },
  favoriteBadge: {
    position: 'absolute',
    top: 4,
    right: 4,
    width: 22,
    height: 22,
    borderRadius: 11,
    backgroundColor: 'rgba(0,0,0,0.55)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  favoriteGlyph: {
    color: '#FF5A7A',
    fontSize: 13,
    lineHeight: 14,
  },
  coverImage: {
    width: '100%',
    height: '100%',
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
  progressRow: {
    marginTop: 6,
    gap: 4,
  },
  progressTrack: {
    height: 6,
    borderRadius: 3,
    overflow: 'hidden',
  },
  progressFill: {
    height: '100%',
    borderRadius: 3,
  },
  progressLabel: {
    fontSize: 12,
    fontWeight: '600',
    fontVariant: ['tabular-nums'],
  },
});
