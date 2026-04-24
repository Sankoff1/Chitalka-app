const { getDefaultConfig } = require('expo/metro-config');

/** @type {import('expo/metro-config').MetroConfig} */
const config = getDefaultConfig(__dirname);

// expo-sqlite on web loads wa-sqlite.wasm; Metro must treat .wasm as a static asset.
config.resolver.assetExts.push('wasm');
// debug: bundled demo EPUB (see src/debug/README.md)
config.resolver.assetExts.push('epub');

module.exports = config;
