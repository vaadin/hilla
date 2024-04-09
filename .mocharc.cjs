const { join } = require('node:path');
const isCI = !!process.env.CI;

const karmaMochaConfig = {
  forbidOnly: isCI,
};

module.exports = {
  extensions: ['ts', 'mts', 'cts', 'js', 'mjs', 'cjs', 'tsx'],
  import: join(__dirname, 'scripts/hooks.js'),
  exit: true,
  karmaMochaConfig,
  ...karmaMochaConfig,
};
