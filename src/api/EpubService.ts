import * as FileSystem from 'expo-file-system/legacy';
import { unzip } from 'react-native-zip-archive';

import { copyFileToInternalStorage } from '../utils/epubPipelineAndroid';
import { withTimeout } from '../utils/withTimeout';

const EPUB_OPEN_LOG = '[Chitalka][Epub]';

/** Сообщение {@link EpubServiceError} при пустом spine (совпадает с проверкой в {@link ReaderScreen}). */
export const EPUB_EMPTY_SPINE = 'EMPTY_SPINE';

function logEpubOpen(step: string, detail?: string): void {
  if (detail) {
    console.log(EPUB_OPEN_LOG, step, detail.length > 900 ? `${detail.slice(0, 900)}…` : detail);
  } else {
    console.log(EPUB_OPEN_LOG, step);
  }
}

const BOOK_CACHE_SEGMENT = 'book_cache/';

/** Таймауты (мс): при зависании нативных/epubjs вызовов UI не остаётся в «вечной» загрузке. */
const TIMEOUT_COPY_MS = 180_000;
const TIMEOUT_UNZIP_MS = 600_000;
const TIMEOUT_PREPARE_CHAPTER_MS = 180_000;

export const EPUB_ERR_TIMEOUT_COPY = 'TIMEOUT_COPY';
export const EPUB_ERR_TIMEOUT_UNZIP = 'TIMEOUT_UNZIP';
export const EPUB_ERR_TIMEOUT_PREPARE_CHAPTER = 'TIMEOUT_PREPARE_CHAPTER';

function escapeHtmlAttrValue(raw: string, quote: string): string {
  let s = raw.replace(/&/g, '&amp;').replace(/</g, '&lt;');
  if (quote === '"') {
    s = s.replace(/"/g, '&quot;');
  } else {
    s = s.replace(/'/g, '&#39;');
  }
  return s;
}

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
  let t = pathOrUri.trim();
  /**
   * epubjs `Url.resolve` для базы `file://…` склеивает `new URL(...).origin` (opaque origin → строка `"null"`)
   * с абсолютным путём → `null/data/user/0/...`. Такие URI Hermes/Android и Expo FileSystem не читают.
   */
  if (t.startsWith('null/')) {
    t = t.slice(4);
  }
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

/** Корневая папка EPUB для epubjs: всегда `file://…/` (три слэша после file: для абсолютного пути на Android). */
function ensureDirectoryRootFileUrl(dirUri: string): string {
  const u = ensureFileUri(dirUri.trim());
  return u.endsWith('/') ? u : `${u}/`;
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

function stripXmlFragment(s: string): string {
  return (s.split('#')[0] ?? '').trim();
}

function decodeBasicXmlEntities(s: string): string {
  return s
    .replace(/&#x([0-9a-fA-F]+);/gi, (_, h) => String.fromCharCode(parseInt(String(h), 16)))
    .replace(/&#(\d+);/g, (_, d) => String.fromCharCode(parseInt(String(d), 10)))
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&apos;/g, "'")
    .replace(/&quot;/g, '"')
    .replace(/&amp;/g, '&')
    .trim();
}

/**
 * Подгонка HTML главы под ширину экрана WebView: без meta viewport страница
 * часто ведёт себя как «широкая» вёрстка и даёт горизонтальную прокрутку.
 */
function injectReaderViewportAndReflowCss(html: string): string {
  const withoutOldViewport = html.replace(
    /<meta\b[^>]*\bname\s*=\s*["']viewport["'][^>]*>/gi,
    ''
  );

  const block = `
<meta name="viewport" content="width=device-width, initial-scale=1">
<style type="text/css" id="chitalka-reader-reflow">
html{-webkit-text-size-adjust:100%;text-size-adjust:100%;}
body{margin:0!important;box-sizing:border-box;width:100%!important;max-width:100vw!important;overflow-x:hidden!important;word-wrap:break-word;overflow-wrap:break-word;}
img,svg,video,object,embed,iframe{max-width:100%!important;height:auto!important;}
table{max-width:100%!important;}
pre,code{white-space:pre-wrap;word-wrap:break-word;max-width:100%;}
</style>
`;

  const headClose = /<\/head>/i.exec(withoutOldViewport);
  if (headClose) {
    const i = headClose.index;
    return withoutOldViewport.slice(0, i) + block + withoutOldViewport.slice(i);
  }

  const headOpen = /<head\b[^>]*>/i.exec(withoutOldViewport);
  if (headOpen) {
    const pos = headOpen.index + headOpen[0].length;
    return withoutOldViewport.slice(0, pos) + block + withoutOldViewport.slice(pos);
  }

  const htmlOpen = /<html\b[^>]*>/i.exec(withoutOldViewport);
  if (htmlOpen) {
    const pos = htmlOpen.index + htmlOpen[0].length;
    return `${withoutOldViewport.slice(0, pos)}<head>${block}</head>${withoutOldViewport.slice(pos)}`;
  }

  return `<!DOCTYPE html><html><head><meta charset="utf-8"/>${block}</head><body>${withoutOldViewport}</body></html>`;
}

function pickDcText(opfXml: string, localName: 'title' | 'creator'): string {
  const re = new RegExp(
    `<(?:dc:${localName}|[a-z]+:${localName})\\b[^>]*>([\\s\\S]*?)<\\/(?:dc:${localName}|[a-z]+:${localName})>`,
    'i'
  );
  const m = re.exec(opfXml);
  if (!m?.[1]) {
    return '';
  }
  return decodeBasicXmlEntities(m[1].replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim());
}

function escapeRegExp(s: string): string {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function joinUnderUnpackedRoot(rootDirFileUrl: string, relativePath: string): string {
  const r = stripXmlFragment(relativePath).replace(/\\/g, '/').replace(/^\/+/, '');
  const base = rootDirFileUrl.endsWith('/') ? rootDirFileUrl : `${rootDirFileUrl}/`;
  try {
    return ensureFileUri(new URL(r, base).href);
  } catch {
    return ensureFileUri(`${base}${r}`);
  }
}

function extractItemHrefById(opfXml: string, itemId: string): string | null {
  const idNeedle = new RegExp(`\\bid\\s*=\\s*["']${escapeRegExp(itemId)}["']`, 'i');
  const itemTagRe = /<item\b[^>]*\/?>/gi;
  let im: RegExpExecArray | null;
  while ((im = itemTagRe.exec(opfXml)) !== null) {
    const tag = im[0];
    if (!idNeedle.test(tag)) {
      continue;
    }
    const hm = /\bhref\s*=\s*["']([^"']+)["']/i.exec(tag);
    const href = hm?.[1]?.trim();
    if (href) {
      return stripXmlFragment(href);
    }
  }
  return null;
}

function extractCoverHrefFromOpf(opfXml: string): string | null {
  let coverItemId: string | null = null;
  const metaTagRe = /<meta\b([^>]*)\/?>/gi;
  let mm: RegExpExecArray | null;
  while ((mm = metaTagRe.exec(opfXml)) !== null) {
    const attrs = mm[1] ?? '';
    if (!/name\s*=\s*["']cover["']/i.test(attrs)) {
      continue;
    }
    const cm = /\bcontent\s*=\s*["']([^"']+)["']/i.exec(attrs);
    if (cm?.[1]) {
      coverItemId = cm[1].trim();
      break;
    }
  }
  if (coverItemId) {
    const href = extractItemHrefById(opfXml, coverItemId);
    if (href) {
      return href;
    }
  }
  const covItem = /<item\b([^>]*properties\s*=\s*["'][^"']*cover-image[^"']*["'][^>]*)\/?>/i.exec(
    opfXml
  );
  if (covItem?.[1]) {
    const hm = /\bhref\s*=\s*["']([^"']+)["']/i.exec(covItem[1]);
    const href = hm?.[1]?.trim();
    if (href) {
      return stripXmlFragment(href);
    }
  }
  const ref = /<reference\b[^>]*type\s*=\s*["']cover["'][^>]*\/?>/i.exec(opfXml);
  if (ref?.[0]) {
    const hm = /\bhref\s*=\s*["']([^"']+)["']/i.exec(ref[0]);
    const href = hm?.[1]?.trim();
    if (href) {
      return stripXmlFragment(href);
    }
  }
  return null;
}

async function readOpfFromUnpackedRoot(
  unpackedRootUri: string
): Promise<{ opfXml: string; opfDirFileUrl: string }> {
  const root = ensureDirectoryRootFileUrl(unpackedRootUri);
  const containerUri = `${root}META-INF/container.xml`;
  const ci = await FileSystem.getInfoAsync(containerUri);
  if (!ci.exists || ci.isDirectory) {
    throw new EpubServiceError('Нет META-INF/container.xml в распакованной книге.');
  }
  const containerXml = await FileSystem.readAsStringAsync(containerUri, {
    encoding: FileSystem.EncodingType.UTF8,
  });
  const fp =
    /\bfull-path\s*=\s*["']([^"']+)["']/i.exec(containerXml)?.[1]?.trim() ??
    /\bfull-path\s*=\s*"([^"]+)"/i.exec(containerXml)?.[1]?.trim();
  if (!fp) {
    throw new EpubServiceError('В container.xml не найден full-path к OPF.');
  }
  const opfUri = joinUnderUnpackedRoot(root, fp);
  const oi = await FileSystem.getInfoAsync(opfUri);
  if (!oi.exists || oi.isDirectory) {
    throw new EpubServiceError(`OPF не найден по пути: ${opfUri}`);
  }
  const opfXml = await FileSystem.readAsStringAsync(opfUri, {
    encoding: FileSystem.EncodingType.UTF8,
  });
  const opfDirUri = opfUri.includes('/')
    ? opfUri.slice(0, opfUri.lastIndexOf('/') + 1)
    : `${root}`;
  return { opfXml, opfDirFileUrl: ensureDirectoryRootFileUrl(opfDirUri) };
}

function extractManifestIdToHrefMap(opfXml: string): Map<string, string> {
  const map = new Map<string, string>();
  const itemRe = /<(?:[\w]*:)?item\b[^>]*>/gi;
  let m: RegExpExecArray | null;
  while ((m = itemRe.exec(opfXml)) !== null) {
    const tag = m[0];
    const idM = /\bid\s*=\s*["']([^"']+)["']/i.exec(tag);
    const hrefM = /\bhref\s*=\s*["']([^"']+)["']/i.exec(tag);
    if (idM?.[1] && hrefM?.[1]) {
      map.set(idM[1].trim(), stripXmlFragment(hrefM[1].trim()));
    }
  }
  return map;
}

function extractSpineItemrefsFromOpf(opfXml: string): { idref: string; linear: boolean }[] {
  const spineBlock = /<(?:[\w]*:)?spine\b[^>]*>([\s\S]*?)<\/(?:[\w]*:)?spine>/i.exec(opfXml);
  if (!spineBlock?.[1]) {
    return [];
  }
  const body = spineBlock[1];
  const out: { idref: string; linear: boolean }[] = [];
  const itemrefRe = /<(?:[\w]*:)?itemref\b([^>]*)\/?>/gi;
  let im: RegExpExecArray | null;
  while ((im = itemrefRe.exec(body)) !== null) {
    const attrs = im[1] ?? '';
    const idrefM = /\bidref\s*=\s*["']([^"']+)["']/i.exec(attrs);
    if (!idrefM?.[1]) {
      continue;
    }
    const idref = idrefM[1].trim();
    const linearM = /\blinear\s*=\s*["']([^"']+)["']/i.exec(attrs);
    const linear = linearM ? linearM[1].toLowerCase() !== 'no' : true;
    out.push({ idref, linear });
  }
  return out;
}

function buildSpineFromOpfXml(opfXml: string): EpubSpineItem[] {
  const manifest = extractManifestIdToHrefMap(opfXml);
  const refs = extractSpineItemrefsFromOpf(opfXml);
  const spine: EpubSpineItem[] = [];
  for (const { idref, linear } of refs) {
    const href = manifest.get(idref);
    if (!href) {
      console.warn(EPUB_OPEN_LOG, 'itemref без href в manifest', idref);
      continue;
    }
    spine.push({ index: spine.length, href, idref, linear });
  }
  return spine;
}

/**
 * Метаданные из OPF после распаковки — только {@link FileSystem}, без epubjs и без системных разрешений.
 */
export async function readFilesystemLibraryMetadata(
  unpackedRootUri: string
): Promise<{ title: string; author: string; coverFileUri: string | null }> {
  try {
    const { opfXml, opfDirFileUrl } = await readOpfFromUnpackedRoot(unpackedRootUri);
    const title = pickDcText(opfXml, 'title');
    const author = pickDcText(opfXml, 'creator');
    const coverRel = extractCoverHrefFromOpf(opfXml);
    if (!coverRel) {
      return { title, author, coverFileUri: null };
    }
    const coverAbs = joinUnderUnpackedRoot(opfDirFileUrl, coverRel);
    const fin = await FileSystem.getInfoAsync(coverAbs);
    if (!fin.exists || fin.isDirectory) {
      return { title, author, coverFileUri: null };
    }
    return { title, author, coverFileUri: coverAbs };
  } catch {
    return { title: '', author: '', coverFileUri: null };
  }
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
 * Сервис распаковки локального EPUB, разбора spine из OPF и подготовки HTML для WebView (без epubjs `book.ready`).
 */
export class EpubService {
  private readonly epubSourceUri: string;

  private unpackedRootUri: string | null = null;

  private opfDirFileUrl = '';

  private spineItems: EpubSpineItem[] = [];

  constructor(epubFilePath: string) {
    this.epubSourceUri = ensureFileUri(epubFilePath);
  }

  getUnpackedRootUri(): string | null {
    return this.unpackedRootUri;
  }

  /**
   * Шаги 1–5: копирование во внутренний кэш и распаковка в `documentDirectory/book_cache/<id>/`.
   * Без epubjs — только {@link FileSystem} и unzip в sandbox приложения.
   */
  async unpackThroughStep5(): Promise<void> {
    if (this.unpackedRootUri) {
      return;
    }
    const base = FileSystem.documentDirectory;
    if (!base) {
      throw new EpubServiceError('documentDirectory недоступен (например, в этом окружении нет хранилища).');
    }

    logEpubOpen('Шаг 1: файл получен', this.epubSourceUri);
    logEpubOpen('Шаг 2: копирование во внутренний кэш (temp.epub)');

    let localEpubUri: string;
    try {
      localEpubUri = await withTimeout(
        copyFileToInternalStorage(this.epubSourceUri),
        TIMEOUT_COPY_MS,
        EPUB_ERR_TIMEOUT_COPY
      );
    } catch (e) {
      const msg = e instanceof Error ? e.message : String(e);
      if (msg === EPUB_ERR_TIMEOUT_COPY) {
        throw new EpubServiceError(EPUB_ERR_TIMEOUT_COPY, e);
      }
      throw new EpubServiceError('Не удалось скопировать EPUB во внутренний кэш (temp.epub).', e);
    }

    logEpubOpen('Шаг 3: скопировано в кэш', localEpubUri);

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

    logEpubOpen('Шаг 4: начинаю распаковку (unzip)');

    const epubPath = fileUriToNativePath(localEpubUri);
    const destPath = fileUriToNativePath(destUri);

    try {
      await withTimeout(unzip(epubPath, destPath), TIMEOUT_UNZIP_MS, EPUB_ERR_TIMEOUT_UNZIP);
    } catch (e) {
      try {
        await FileSystem.deleteAsync(destUri, { idempotent: true });
      } catch {
        /* ignore */
      }
      const unzipMsg = e instanceof Error ? e.message : String(e);
      if (unzipMsg === EPUB_ERR_TIMEOUT_UNZIP) {
        throw new EpubServiceError(EPUB_ERR_TIMEOUT_UNZIP, e);
      }
      throw new EpubServiceError(
        'Не удалось распаковать EPUB. Файл может быть повреждён или не являться ZIP/EPUB.',
        e
      );
    }

    const destRootForProbe = ensureDirectoryRootFileUrl(destUri);
    const containerUri = `${destRootForProbe}META-INF/container.xml`;
    let containerOk = false;
    try {
      const ci = await FileSystem.getInfoAsync(containerUri);
      containerOk = ci.exists && !ci.isDirectory;
    } catch {
      containerOk = false;
    }
    if (!containerOk) {
      throw new EpubServiceError('Папка после распаковки пуста или нет META-INF/container.xml.');
    }

    this.unpackedRootUri = destUri;

    logEpubOpen('Шаг 5: распаковка завершена', destRootForProbe);
  }

  /**
   * Распаковывает EPUB в `documentDirectory/book_cache/<id>/`, читает OPF и возвращает spine (оглавление пока пустое).
   */
  async open(): Promise<EpubStructure> {
    await this.unpackThroughStep5();
    const destUri = this.unpackedRootUri;
    if (!destUri) {
      throw new EpubServiceError('Внутренняя ошибка: нет каталога распаковки после unzip.');
    }

    const epubRootFileUrl = ensureDirectoryRootFileUrl(destUri);
    logEpubOpen('Шаг 6: разбор OPF (без epubjs book.ready)', epubRootFileUrl);

    let opfXml: string;
    let opfDir: string;
    try {
      const opf = await readOpfFromUnpackedRoot(destUri);
      opfXml = opf.opfXml;
      opfDir = opf.opfDirFileUrl;
    } catch (e) {
      throw new EpubServiceError(
        'Не удалось прочитать container.xml или OPF. Файл может быть повреждён.',
        e
      );
    }

    const spine = buildSpineFromOpfXml(opfXml);
    if (!spine.length) {
      throw new EpubServiceError(EPUB_EMPTY_SPINE);
    }

    this.opfDirFileUrl = opfDir;
    this.spineItems = spine;

    logEpubOpen('Шаг 7: spine готов', `${spine.length} элементов`);

    return {
      spine,
      toc: [],
      unpackedRootUri: destUri,
    };
  }

  /**
   * Абсолютный URL файла главы по индексу в spine (после {@link open}).
   * Подходит для {@link prepareChapter} и для `baseUrl` во WebView.
   */
  getSpineChapterUri(spineIndex: number): string {
    if (!this.unpackedRootUri || !this.opfDirFileUrl) {
      throw new EpubServiceError('Сначала вызовите open() для распаковки и разбора книги.');
    }
    const item = this.spineItems[spineIndex];
    if (!item) {
      throw new EpubServiceError(`Нет элемента spine с индексом ${spineIndex}.`);
    }
    return ensureFileUri(joinUnderUnpackedRoot(this.opfDirFileUrl, item.href));
  }

  /**
   * Читает HTML/XHTML главы и подставляет в `img` абсолютные `file://` URI локальных ресурсов
   * (без Base64 — иначе крупные главы «висят» на чтении и кодировании сотен картинок).
   */
  async prepareChapter(htmlPath: string): Promise<string> {
    try {
      return await withTimeout(
        this.prepareChapterBody(htmlPath),
        TIMEOUT_PREPARE_CHAPTER_MS,
        EPUB_ERR_TIMEOUT_PREPARE_CHAPTER
      );
    } catch (e) {
      const em = e instanceof Error ? e.message : String(e);
      if (em === EPUB_ERR_TIMEOUT_PREPARE_CHAPTER) {
        throw new EpubServiceError(EPUB_ERR_TIMEOUT_PREPARE_CHAPTER, e);
      }
      throw e;
    }
  }

  private async prepareChapterBody(htmlPath: string): Promise<string> {
    if (!this.unpackedRootUri) {
      throw new EpubServiceError('Сначала вызовите open() для распаковки и разбора книги.');
    }

    const chapterUri = ensureFileUri(htmlPath);
    let html: string;
    try {
      html = await FileSystem.readAsStringAsync(chapterUri, {
        encoding: FileSystem.EncodingType.UTF8,
      });
    } catch (err) {
      throw new EpubServiceError(`Не удалось прочитать главу: ${chapterUri}`, err);
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
      const newTag = await this.rewriteLocalImageSrcInTag(
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

    return injectReaderViewportAndReflowCss(html);
  }

  private async rewriteLocalImageSrcInTag(
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

    const escaped = escapeHtmlAttrValue(assetUri, quote);
    const newSrcAttr = `src=${quote}${escaped}${quote}`;
    return fullTag.replace(srcAttrFull, newSrcAttr);
  }

  /**
   * Название и автор из OPF (после {@link open}).
   */
  async getMetadata(): Promise<{ title: string; author: string }> {
    if (!this.unpackedRootUri) {
      throw new EpubServiceError('Сначала вызовите open() для распаковки и разбора книги.');
    }
    const m = await readFilesystemLibraryMetadata(this.unpackedRootUri);
    return { title: m.title, author: m.author };
  }

  /**
   * Абсолютный `file://` URI файла обложки в распакованном каталоге, либо `null`.
   */
  async resolveCoverFileUri(): Promise<string | null> {
    if (!this.unpackedRootUri) {
      throw new EpubServiceError('Сначала вызовите open() для распаковки и разбора книги.');
    }
    const m = await readFilesystemLibraryMetadata(this.unpackedRootUri);
    return m.coverFileUri;
  }

  destroy(): void {
    this.spineItems = [];
    this.opfDirFileUrl = '';
    this.unpackedRootUri = null;
  }
}
