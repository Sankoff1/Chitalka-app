import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useCallback } from 'react';

import { useLibrary } from '../context/LibraryContext';
import { ReaderScreen } from '../screens/ReaderScreen';
import type { RootStackParamList } from './types';

type Props = NativeStackScreenProps<RootStackParamList, 'Reader'>;

export function ReaderScreenWrapper({ route, navigation }: Props) {
  const { refreshBookCount } = useLibrary();
  const { bookPath, bookId } = route.params;

  const onBackToLibrary = useCallback(() => {
    void refreshBookCount();
    navigation.goBack();
  }, [navigation, refreshBookCount]);

  const onOpened = useCallback(() => {
    void refreshBookCount();
  }, [refreshBookCount]);

  return (
    <ReaderScreen
      bookPath={bookPath}
      bookId={bookId}
      onBackToLibrary={onBackToLibrary}
      onOpened={onOpened}
    />
  );
}
