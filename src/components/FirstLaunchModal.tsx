import { Modal, Pressable, StyleSheet, Text, View } from 'react-native';

import { useI18n } from '../i18n';
import { useTheme } from '../theme';

type FirstLaunchModalProps = {
  visible: boolean;
  hint: string | null;
  onDismiss: () => void;
  onPickEpub: () => void;
};

export function FirstLaunchModal({
  visible,
  hint,
  onDismiss,
  onPickEpub,
}: FirstLaunchModalProps) {
  const { colors } = useTheme();
  const { t } = useI18n();

  return (
    <Modal visible={visible} transparent animationType="fade">
      <View style={styles.overlay}>
        <View
          style={[
            styles.card,
            {
              backgroundColor: colors.menuBackground,
              borderColor: colors.textSecondary + '44',
            },
          ]}
        >
          <Text style={[styles.message, { color: colors.text }]}>
            {t('firstLaunch.message')}
          </Text>
          {hint ? (
            <Text style={[styles.hint, { color: '#a33' }]}>{hint}</Text>
          ) : null}
          <View style={styles.buttons}>
            <Pressable
              onPress={onDismiss}
              style={({ pressed }) => [
                styles.button,
                styles.buttonSecondary,
                {
                  borderColor: colors.textSecondary,
                  backgroundColor: colors.background,
                },
                pressed && styles.pressed,
              ]}
            >
              <Text style={[styles.buttonSecondaryText, { color: colors.text }]}>
                {t('firstLaunch.cancel')}
              </Text>
            </Pressable>
            <Pressable
              onPress={onPickEpub}
              style={({ pressed }) => [
                styles.button,
                styles.buttonPrimary,
                { backgroundColor: colors.interactive },
                pressed && styles.pressed,
              ]}
            >
              <Text style={[styles.buttonPrimaryText, { color: colors.text }]}>
                {t('firstLaunch.pickEpub')}
              </Text>
            </Pressable>
          </View>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.45)',
    justifyContent: 'center',
    paddingHorizontal: 28,
  },
  card: {
    borderRadius: 14,
    padding: 22,
    borderWidth: StyleSheet.hairlineWidth,
  },
  message: {
    fontSize: 16,
    lineHeight: 23,
  },
  hint: {
    marginTop: 12,
    fontSize: 14,
    lineHeight: 20,
  },
  buttons: {
    flexDirection: 'row',
    gap: 12,
    marginTop: 22,
  },
  button: {
    flex: 1,
    paddingVertical: 14,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  buttonPrimary: {},
  buttonSecondary: {
    borderWidth: 1,
  },
  pressed: {
    opacity: 0.88,
  },
  buttonPrimaryText: {
    fontSize: 16,
    fontWeight: '600',
  },
  buttonSecondaryText: {
    fontSize: 16,
    fontWeight: '600',
  },
});
