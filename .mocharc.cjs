const {pathToFileURL} = require("node:url");
const isCI = !!process.env.CI;

const karmaMochaConfig = {
  forbidOnly: isCI,
};

const root = pathToFileURL(`${__dirname}/`);

module.exports = {
  extensions: ['ts', 'mts', 'cts', 'js', 'mjs', 'cjs', 'tsx'],
  import: new URL('scripts/hooks.js', root),
  exit: true,
  karmaMochaConfig,
  ...karmaMochaConfig,
};
