const { join } = require('node:path');
const isCI = !!process.env.CI;

const karmaMochaConfig = {
  forbidOnly: isCI,
};

module.exports = {
  extensions: ['ts', 'mts', 'cts', 'js', 'mjs', 'cjs'],
  import: join(__dirname, 'scripts/hooks.js'),
  exit: true,
  verbose: true,
  karmaMochaConfig,
  ...karmaMochaConfig,
};
