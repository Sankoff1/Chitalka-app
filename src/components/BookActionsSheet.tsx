import MaterialIcons from '@expo/vector-icons/MaterialIcons';
import { useEffect, useMemo, useRef } from 'react';
import {
  Animated,
  Easing,
  Image,
  Modal,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { useI18n } from '../i18n';
import { useTheme } from '../theme';

export type BookActionItem = {
  key: string;
  label: string;
  icon: React.ComponentProps<typeof MaterialIcons>['name'];
  destructive?: boolean;
  onPress: () => void;
};

export type BookActionsSheetProps = {
  visible: boolean;
  title: string;
  author: string;
  coverUri?: string | null;
  actions: BookActionItem[];
  onClose: () => void;
};

export function BookActionsSheet({
  visible,
  title,
  author,
  coverUri,
  actions,
  onClose,
}: BookActionsSheetProps) {
  const { colors } = useTheme();
  const { t } = useI18n();
  const insets = useSafeAreaInsets();
  const anim = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    if (visible) {
      Animated.timing(anim, {
        toValue: 1,
        duration: 220,
        easing: Easing.out(Easing.cubic),
        useNativeDriver: true,
      }).start();
    } else {
      anim.setValue(0);
    }
  }, [anim, visible]);

  const translateY = anim.interpolate({
    inputRange: [0, 1],
    outputRange: [60, 0],
  });
  const backdropOpacity = anim.interpolate({
    inputRange: [0, 1],
    outputRange: [0, 0.55],
  });

  const destructiveColor = '#D93A3A';
  const separatorColor = useMemo(
    () => `${colors.textSecondary}26`,
    [colors.textSecondary]
  );

  return (
    <Modal
      visible={visible}
      transparent
      animationType="none"
      onRequestClose={onClose}
      statusBarTranslucent
    >
      <View style={styles.root}>
        <Animated.View
          pointerEvents={visible ? 'auto' : 'none'}
          style={[
            styles.backdrop,
            { opacity: backdropOpacity, backgroundColor: '#000' },
          ]}
        >
          <Pressable style={StyleSheet.absoluteFill} onPress={onClose} />
        </Animated.View>
        <Animated.View
          style={[
            styles.sheet,
            {
              backgroundColor: colors.menuBackground,
              paddingBottom: Math.max(insets.bottom, 12) + 8,
              transform: [{ translateY }],
              opacity: anim,
            },
          ]}
        >
          <View
            style={[styles.grabber, { backgroundColor: separatorColor }]}
            accessibilityElementsHidden
          />
          <View style={styles.header}>
            <View
              style={[
                styles.coverWrap,
                { backgroundColor: colors.background },
              ]}
            >
              {coverUri ? (
                <Image
                  source={{ uri: coverUri }}
                  style={styles.coverImage}
                  resizeMode="cover"
                />
              ) : (
                <Text style={[styles.coverGlyph, { color: colors.textSecondary }]}>
                  📖
                </Text>
              )}
            </View>
            <View style={styles.headerText}>
              <Text
                numberOfLines={2}
                style={[styles.title, { color: colors.text }]}
              >
                {title}
              </Text>
              <Text
                numberOfLines={1}
                style={[styles.author, { color: colors.textSecondary }]}
              >
                {author}
              </Text>
            </View>
          </View>

          <View
            style={[styles.divider, { backgroundColor: separatorColor }]}
          />

          <View style={styles.actions}>
            {actions.map((action) => {
              const tint = action.destructive ? destructiveColor : colors.text;
              return (
                <Pressable
                  key={action.key}
                  onPress={() => {
                    onClose();
                    action.onPress();
                  }}
                  android_ripple={{ color: `${colors.interactive}55` }}
                  style={({ pressed }) => [
                    styles.actionRow,
                    pressed && { backgroundColor: `${colors.interactive}33` },
                  ]}
                >
                  <MaterialIcons
                    name={action.icon}
                    size={22}
                    color={tint}
                    style={styles.actionIcon}
                  />
                  <Text style={[styles.actionLabel, { color: tint }]}>
                    {action.label}
                  </Text>
                </Pressable>
              );
            })}
          </View>

          <Pressable
            onPress={onClose}
            android_ripple={{ color: `${colors.interactive}55` }}
            style={({ pressed }) => [
              styles.cancel,
              { backgroundColor: colors.interactive },
              pressed && { opacity: 0.85 },
            ]}
          >
            <Text style={[styles.cancelLabel, { color: colors.text }]}>
              {t('common.cancel')}
            </Text>
          </Pressable>
        </Animated.View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  backdrop: {
    ...StyleSheet.absoluteFillObject,
  },
  sheet: {
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    paddingTop: 8,
    paddingHorizontal: 16,
  },
  grabber: {
    alignSelf: 'center',
    width: 40,
    height: 4,
    borderRadius: 2,
    marginBottom: 12,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    paddingVertical: 4,
  },
  coverWrap: {
    width: 48,
    height: 68,
    borderRadius: 6,
    overflow: 'hidden',
    alignItems: 'center',
    justifyContent: 'center',
  },
  coverImage: {
    width: '100%',
    height: '100%',
  },
  coverGlyph: {
    fontSize: 22,
  },
  headerText: {
    flex: 1,
    gap: 2,
  },
  title: {
    fontSize: 16,
    fontWeight: '700',
    lineHeight: 20,
  },
  author: {
    fontSize: 13,
    lineHeight: 18,
  },
  divider: {
    height: StyleSheet.hairlineWidth,
    marginTop: 12,
    marginBottom: 4,
  },
  actions: {
    paddingVertical: 4,
  },
  actionRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 14,
    paddingHorizontal: 8,
    borderRadius: 10,
  },
  actionIcon: {
    width: 28,
  },
  actionLabel: {
    fontSize: 15,
    fontWeight: '500',
  },
  cancel: {
    marginTop: 8,
    paddingVertical: 14,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
  cancelLabel: {
    fontSize: 15,
    fontWeight: '600',
  },
});
