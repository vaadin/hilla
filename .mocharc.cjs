const { resolve } = require('path');
const { pathToFileURL } = require('url');

module.exports = {
  extension: ['ts', 'mts', 'cts', 'js', 'mjs', 'cjs'],
  loader: pathToFileURL(resolve(__dirname, 'scripts/node/esbuild.js')),
};
