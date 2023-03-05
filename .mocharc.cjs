const { pathToFileURL } = require('node:url');

module.exports = {
  extension: ['ts', 'mts', 'cts', 'js', 'mjs', 'cjs'],
  loader: pathToFileURL(require.resolve('tsx')),
};
