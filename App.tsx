import { StatusBar } from 'expo-status-bar';
import { useCallback, useState } from 'react';
import { StyleSheet, View } from 'react-native';

import { LibraryScreen } from './src/screens/LibraryScreen';
import { ReaderScreen } from './src/screens/ReaderScreen';

type SelectedBook = {
  uri: string;
  bookId: string;
};

export default function App() {
  const [selectedBook, setSelectedBook] = useState<SelectedBook | null>(null);

  const handleBookSelected = useCallback((uri: string, bookId: string) => {
    setSelectedBook({ uri, bookId });
  }, []);

  const handleBackToLibrary = useCallback(() => {
    setSelectedBook(null);
  }, []);

  return (
    <View style={styles.root}>
      <StatusBar style="dark" />
      {selectedBook ? (
        <ReaderScreen
          bookPath={selectedBook.uri}
          bookId={selectedBook.bookId}
          onBackToLibrary={handleBackToLibrary}
        />
      ) : (
        <LibraryScreen onBookSelected={handleBookSelected} />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
  },
});
