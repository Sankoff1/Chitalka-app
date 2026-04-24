import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import { useCallback, useEffect } from 'react';

import { useLibrary } from '../context/LibraryContext';
import { clearLastOpenBookId, setLastOpenBookId } from '../library/lastOpenBook';
import { ReaderScreen } from '../screens/ReaderScreen';
import type { RootStackParamList } from './types';

type Props = NativeStackScreenProps<RootStackParamList, 'Reader'>;

export function ReaderScreenWrapper({ route, navigation }: Props) {
  const { refreshBookCount } = useLibrary();
  const { bookPath, bookId } = route.params;

  /* Помним id открытой книги — используется для автооткрытия на следующем запуске.
     Писать на mount, чистить строго по событию навигации `beforeRemove` (back/goBack):
     при JS-reload и kill процесса событие не срабатывает, и ключ корректно переживает
     перезапуск. На обычный unmount завязываться нельзя — JS-reload демонтирует экран
     штатно, cleanup стёр бы ключ. */
  useEffect(() => {
    void setLastOpenBookId(bookId);
  }, [bookId]);

  useEffect(() => {
    const unsubscribe = navigation.addListener('beforeRemove', () => {
      void clearLastOpenBookId();
    });
    return unsubscribe;
  }, [navigation]);

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
