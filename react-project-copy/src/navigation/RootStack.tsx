import { createNativeStackNavigator } from '@react-navigation/native-stack';

import { AppDrawer } from './AppDrawer';
import { ReaderScreenWrapper } from './ReaderScreenWrapper';
import type { RootStackParamList } from './types';

const Stack = createNativeStackNavigator<RootStackParamList>();

export function RootStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="Main" component={AppDrawer} />
      <Stack.Screen name="Reader" component={ReaderScreenWrapper} />
    </Stack.Navigator>
  );
}
