import * as FileSystem from 'expo-file-system/legacy';

/**
 * Копирует входной URI во внутренний кэш `cacheDirectory/temp.epub` (в т.ч. с `content://`).
 * Дальше работаем только с получившимся `file://` — без запросов разрешений к внешнему хранилищу.
 */
export async function copyFileToInternalStorage(sourceUri: string): Promise<string> {
  const cache = FileSystem.cacheDirectory;
  if (!cache) {
    throw new Error('cacheDirectory недоступен');
  }
  const base = cache.endsWith('/') ? cache : `${cache}/`;
  const dest = `${base}temp.epub`;
  await FileSystem.copyAsync({ from: sourceUri.trim(), to: dest });
  const info = await FileSystem.getInfoAsync(dest);
  if (!info.exists || info.isDirectory) {
    throw new Error('После copyAsync файл temp.epub не найден или это каталог');
  }
  return dest;
}
