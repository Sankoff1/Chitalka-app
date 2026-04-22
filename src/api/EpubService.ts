import ePub, { Book, NavItem } from 'epubjs';
import * as FileSystem from 'expo-file-system/legacy';
import type Section from 'epubjs/types/section';
import { unzip } from 'react-native-zip-archive';

type EpubCoreUtils = {
  parse: (markup: string, mime: string, forceXMLDom?: boolean) => Document;
  isXml: (ext: string) => boolean;
};

// eslint-disable-next-line @typescript-eslint/no-require-imports
const { parse, isXml } = require('epubjs/lib/utils/core') as EpubCoreUtils;

const BOOK_CACHE_SEGMENT = 'book_cache/';

export class EpubServiceError extends Error {
  constructor(
    message: string,
    public readonly cause?: unknown
  ) {
    super(message);
    this.name = 'EpubServiceError';
  }
}

export interface EpubSpineItem {
  index: number;
  href: string;
  idref: string;
  linear: boolean;
}

export interface EpubTocItem {
  id: string;
  href: string;
  label: string;
  subitems: EpubTocItem[];
}

export interface EpubStructure {
  spine: EpubSpineItem[];
  toc: EpubTocItem[];
  /** Корень распакованной книги (`file://.../book_cache/<id>/`). */
  unpackedRootUri: string;
}

function ensureFileUri(pathOrUri: string): string {
  const t = pathOrUri.trim();
  if (t.startsWith('file://')) {
    return t;
  }
  if (t.startsWith('content://')) {
    return t;
  }
  const normalized = t.replace(/\\/g, '/');
  if (normalized.startsWith('/')) {
    return `file://${normalized}`;
  }
  if (/^[a-zA-Z]:/.test(normalized)) {
    const rest = normalized.replace(/^\/+/, '');
    return `file:///${rest}`;
  }
  return `file:///${normalized}`;
}

/** Путь для нативных модулей (unzip и т.д.). */
function fileUriToNativePath(uri: string): string {
  let path = uri.trim();
  if (path.startsWith('file://')) {
    path = path.replace(/^file:\/\//, '');
    if (/^\/[a-zA-Z]:/.test(path)) {
      path = path.slice(1);
    }
  }
  return decodeURIComponent(path);
}

function getExtensionFromUrl(url: string): string {
  const pathOnly = url.split('?')[0] ?? url;
  const parts = pathOnly.split('.');
  if (parts.length < 2) {
    return '';
  }
  return (parts.pop() ?? '').toLowerCase();
}

function createEpubFileRequest(): (
  url: string,
  type: string,
  _withCredentials?: object,
  _headers?: object
) => Promise<object> {
  return async (url, type) => {
    const uri = url.trim();
    const ext = (type && type !== 'binary' ? type : getExtensionFromUrl(uri)).toLowerCase();

    const info = await FileSystem.getInfoAsync(uri);
    if (!info.exists) {
      throw new Error(`Resource not found: ${uri}`);
    }

    const readUtf8 = () => FileSystem.readAsStringAsync(uri, { encoding: FileSystem.EncodingType.UTF8 });
    const readB64 = () => FileSystem.readAsStringAsync(uri, { encoding: FileSystem.EncodingType.Base64 });

    if (isXml(ext)) {
      const text = await readUtf8();
      return parse(text, 'text/xml') as unknown as object;
    }

    if (ext === 'xhtml') {
      const text = await readUtf8();
      return parse(text, 'application/xhtml+xml') as unknown as object;
    }

    if (ext === 'html' || ext === 'htm') {
      const text = await readUtf8();
      return parse(text, 'text/html') as unknown as object;
    }

    if (ext === 'json') {
      const text = await readUtf8();
      return JSON.parse(text) as object;
    }

    if (ext === 'css' || ext === 'svg' || ext === 'txt' || ext === 'xml') {
      return (await readUtf8()) as unknown as object;
    }

    const binaryExt = new Set([
      'png',
      'jpg',
      'jpeg',
      'gif',
      'webp',
      'bmp',
      'ico',
      'woff',
      'woff2',
      'ttf',
      'otf',
      'eot',
      'mp3',
      'mp4',
      'opus',
    ]);

    if (ext === 'binary' || ext === 'blob' || binaryExt.has(ext)) {
      return (await readB64()) as unknown as object;
    }

    const text = await readUtf8();
    if (text.trimStart().startsWith('<')) {
      try {
        return parse(text, 'text/xml') as unknown as object;
      } catch {
        return text as unknown as object;
      }
    }
    return text as unknown as object;
  };
}

function cloneToc(items: NavItem[] | undefined): EpubTocItem[] {
  if (!items?.length) {
    return [];
  }
  return items.map((n) => ({
    id: n.id,
    href: n.href,
    label: n.label,
    subitems: cloneToc(n.subitems ?? []),
  }));
}

function mimeFromPath(uri: string): string {
  const ext = getExtensionFromUrl(uri);
  const map: Record<string, string> = {
    png: 'image/png',
    jpg: 'image/jpeg',
    jpeg: 'image/jpeg',
    gif: 'image/gif',
    webp: 'image/webp',
    bmp: 'image/bmp',
    svg: 'image/svg+xml',
    ico: 'image/x-icon',
  };
  return map[ext] ?? 'application/octet-stream';
}

function resolveChapterAssetUri(
  unpackedRootUri: string,
  chapterFileUri: string,
  src: string
): string {
  const raw = src.trim();
  const [pathPart] = raw.split('#');
  const clean = (pathPart ?? '').trim();
  if (!clean) {
    return '';
  }

  const root = unpackedRootUri.endsWith('/') ? unpackedRootUri : `${unpackedRootUri}/`;

  try {
    if (clean.startsWith('/')) {
      return new URL(clean.slice(1), root).href;
    }
    return new URL(clean, chapterFileUri).href;
  } catch {
    return '';
  }
}

/**
 * Сервис распаковки локального EPUB, разбора структуры через epubjs и подготовки HTML для WebView.
 */
export class EpubService {
  private readonly epubSourceUri: string;

  private unpackedRootUri: string | null = null;

  private book: Book | null = null;

  constructor(epubFilePath: string) {
    this.epubSourceUri = ensureFileUri(epubFilePath);
  }

  getUnpackedRootUri(): string | null {
    return this.unpackedRootUri;
  }

  /**
   * Гарантирует, что EPUB доступен как локальный `file://` путь для нативной распаковки.
   * `content://` (SAF и т.п.) копируется в {@link FileSystem.cacheDirectory} через {@link FileSystem.copyAsync}.
   * Уже существующий `file://` возвращается без копирования.
   */
  async ensureLocalCopy(uri: string): Promise<string> {
    const t = uri.trim();
    if (t.startsWith('content://')) {
      const cache = FileSystem.cacheDirectory;
      if (!cache) {
        throw new EpubServiceError(
          'cacheDirectory недоступен — нельзя скопировать файл по content:// в локальное хранилище.'
        );
      }
      const extractId =
        globalThis.crypto && 'randomUUID' in globalThis.crypto
          ? globalThis.crypto.randomUUID()
          : `${Date.now()}_${Math.random().toString(36).slice(2, 11)}`;
      const destUri = `${cache}epub_import_${extractId}.epub`;
      try {
        await FileSystem.copyAsync({ from: t, to: destUri });
      } catch (e) {
        throw new EpubServiceError(
          'Не удалось скопировать EPUB из content:// в каталог кэша приложения.',
          e
        );
      }
      return destUri;
    }
    if (t.startsWith('file://')) {
      return t;
    }
    return ensureFileUri(t);
  }

  /**
   * Распаковывает EPUB в `documentDirectory + book_cache/<уникально>/`, открывает книгу epubjs и возвращает spine и оглавление.
   */
  async open(): Promise<EpubStructure> {
    const base = FileSystem.documentDirectory;
    if (!base) {
      throw new EpubServiceError('documentDirectory недоступен (например, в этом окружении нет хранилища).');
    }

    const localEpubUri = await this.ensureLocalCopy(this.epubSourceUri);

    const cacheRoot = `${base}${BOOK_CACHE_SEGMENT}`;
    const extractId =
      globalThis.crypto && 'randomUUID' in globalThis.crypto
        ? globalThis.crypto.randomUUID()
        : `${Date.now()}_${Math.random().toString(36).slice(2, 11)}`;
    const destUri = `${cacheRoot}${extractId}/`;

    try {
      await FileSystem.makeDirectoryAsync(destUri, { intermediates: true });
    } catch (e) {
      throw new EpubServiceError('Не удалось создать каталог для распаковки.', e);
    }

    const epubPath = fileUriToNativePath(localEpubUri);
    const destPath = fileUriToNativePath(destUri);

    try {
      await unzip(epubPath, destPath);
    } catch (e) {
      try {
        await FileSystem.deleteAsync(destUri, { idempotent: true });
      } catch {
        /* ignore */
      }
      throw new EpubServiceError(
        'Не удалось распаковать EPUB. Файл может быть повреждён или не являться ZIP/EPUB.',
        e
      );
    }

    this.unpackedRootUri = destUri;

    const directoryUrl = destUri.endsWith('/') ? destUri : `${destUri}/`;

    try {
      const book = ePub(directoryUrl, {
        openAs: 'directory',
        replacements: 'none',
        requestMethod: createEpubFileRequest(),
      });
      this.book = book;
      await book.ready;
    } catch (e) {
      this.book = null;
      throw new EpubServiceError(
        'Не удалось разобрать структуру EPUB (container.opf, навигация или манифест). Файл может быть повреждён.',
        e
      );
    }

    const spine: EpubSpineItem[] = [];
    this.book.spine.each((section: Section) => {
      spine.push({
        index: section.index,
        href: section.href,
        idref: section.idref,
        linear: section.linear,
      });
    });

    const toc = cloneToc(this.book.navigation?.toc);

    return {
      spine,
      toc,
      unpackedRootUri: destUri,
    };
  }

  /**
   * Читает HTML/XHTML главы, встраивает локальные изображения как data URL (Base64) для отображения во WebView.
   * `htmlPath` — абсолютный путь к файлу главы (`file://...` или путь ОС).
   */
  /**
   * Абсолютный URL файла главы по индексу в spine (после {@link open}).
   * Подходит для {@link prepareChapter} и для `baseUrl` во WebView.
   */
  getSpineChapterUri(spineIndex: number): string {
    if (!this.book) {
      throw new EpubServiceError('Сначала вызовите open() для распаковки и разбора книги.');
    }
    const section = this.book.spine.get(spineIndex) as Section | undefined;
    if (!section?.url) {
      throw new EpubServiceError(`Нет элемента spine с индексом ${spineIndex}.`);
    }
    return section.url;
  }

  async prepareChapter(htmlPath: string): Promise<string> {
    if (!this.unpackedRootUri) {
      throw new EpubServiceError('Сначала вызовите open() для распаковки и разбора книги.');
    }

    const chapterUri = ensureFileUri(htmlPath);
    let html: string;
    try {
      html = await FileSystem.readAsStringAsync(chapterUri, {
        encoding: FileSystem.EncodingType.UTF8,
      });
    } catch (e) {
      throw new EpubServiceError(`Не удалось прочитать главу: ${chapterUri}`, e);
    }

    const imgTagRe = /<img\b[^>]*>/gi;
    const replacements: { start: number; end: number; text: string }[] = [];

    let match: RegExpExecArray | null;
    while ((match = imgTagRe.exec(html)) !== null) {
      const fullTag = match[0];
      const srcMatch = /\bsrc\s*=\s*(["'])([\s\S]*?)\1/i.exec(fullTag);
      if (!srcMatch) {
        continue;
      }
      const quote = srcMatch[1];
      const srcVal = srcMatch[2].trim();
      const newTag = await this.embedLocalImageInTag(
        fullTag,
        srcMatch[0],
        quote,
        srcVal,
        chapterUri
      );
      replacements.push({
        start: match.index,
        end: match.index + fullTag.length,
        text: newTag,
      });
    }

    replacements.sort((a, b) => b.start - a.start);
    for (const r of replacements) {
      html = html.slice(0, r.start) + r.text + html.slice(r.end);
    }

    return html;
  }

  private async embedLocalImageInTag(
    fullTag: string,
    srcAttrFull: string,
    quote: string,
    srcVal: string,
    chapterUri: string
  ): Promise<string> {
    if (!srcVal || srcVal.startsWith('data:')) {
      return fullTag;
    }
    if (/^https?:\/\//i.test(srcVal)) {
      return fullTag;
    }

    const root = this.unpackedRootUri;
    if (!root) {
      return fullTag;
    }

    const assetUri = resolveChapterAssetUri(root, chapterUri, srcVal);
    if (!assetUri.startsWith('file://')) {
      return fullTag;
    }

    let info: FileSystem.FileInfo;
    try {
      info = await FileSystem.getInfoAsync(assetUri);
    } catch {
      return fullTag;
    }
    if (!info.exists || info.isDirectory) {
      return fullTag;
    }

    try {
      const b64 = await FileSystem.readAsStringAsync(assetUri, {
        encoding: FileSystem.EncodingType.Base64,
      });
      const mime = mimeFromPath(assetUri);
      const dataUrl = `data:${mime};base64,${b64}`;
      const newSrcAttr = `src=${quote}${dataUrl}${quote}`;
      return fullTag.replace(srcAttrFull, newSrcAttr);
    } catch {
      return fullTag;
    }
  }

  destroy(): void {
    try {
      this.book?.destroy();
    } catch {
      /* ignore */
    }
    this.book = null;
    this.unpackedRootUri = null;
  }
}
