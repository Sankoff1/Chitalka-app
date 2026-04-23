import type { NavigatorScreenParams } from '@react-navigation/native';

export type DrawerParamList = {
  ReadingNow: undefined;
  BooksAndDocs: undefined;
  Favorites: undefined;
  Authors: undefined;
  Collections: undefined;
  Cart: undefined;
  DebugLogs: undefined;
  Settings: undefined;
};

export type RootStackParamList = {
  Main: NavigatorScreenParams<DrawerParamList> | undefined;
  Reader: { bookPath: string; bookId: string };
};
