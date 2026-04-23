import * as DocumentPicker from 'expo-document-picker';
import { Platform } from 'react-native';

export function deriveBookId(fileName: string): string {
  const base = fileName.replace(/^.*[/\\]/, '').trim();
  const withoutExt = base.replace(/\.epub$/i, '').trim();
  return withoutExt.length > 0 ? withoutExt : `book_${Date.now()}`;
}

export function isEpubFileName(name: string): boolean {
  return name.trim().toLowerCase().endsWith('.epub');
}

export type EpubPickResult =
  | { kind: 'ok'; uri: string; bookId: string }
  | { kind: 'canceled' }
  | { kind: 'error'; messageKey: string };

function isLikelyEpubAsset(asset: {
  name: string;
  uri: string;
  mimeType?: string;
}): boolean {
  if (isEpubFileName(asset.name)) {
    return true;
  }
  const mime = asset.mimeType?.trim().toLowerCase() ?? '';
  if (mime.includes('epub')) {
    return true;
  }
  const pathOnly = asset.uri.split('?')[0] ?? asset.uri;
  return /\.epub$/i.test(pathOnly);
}

const EPUB_PICK_TYPES: string | string[] =
  Platform.OS === 'android'
    ? [
        'application/epub+zip',
        'application/octet-stream',
        'application/x-fictionbook+xml',
        '*/*',
      ]
    : 'application/epub+zip';

export async function pickEpubAsset(): Promise<EpubPickResult> {
  try {
    const result = await DocumentPicker.getDocumentAsync({
      type: EPUB_PICK_TYPES,
      /** Не полагаемся на кэш Expo: на Android надёжнее `content://` + своя копия в `documentDirectory`. */
      copyToCacheDirectory: false,
      multiple: false,
    });

    if (result.canceled) {
      return { kind: 'canceled' };
    }

    const asset = result.assets?.[0];
    const uri = asset?.uri?.trim() ?? '';
    if (!asset || !uri) {
      return { kind: 'canceled' };
    }

    if (!isLikelyEpubAsset(asset)) {
      return {
        kind: 'error',
        messageKey: 'picker.invalidExtension',
      };
    }

    const nameForId =
      asset.name?.trim() ||
      decodeURIComponent(uri.split('/').pop()?.split('?')[0] ?? '') ||
      'book.epub';

    return {
      kind: 'ok',
      uri,
      bookId: deriveBookId(nameForId),
    };
  } catch {
    return {
      kind: 'error',
      messageKey: 'picker.openFailed',
    };
  }
}
