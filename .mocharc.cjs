const isCI = !!process.env.CI;

const karmaMochaConfig = {
  forbidOnly: isCI,
}

module.exports = {
  extensions: ['ts', 'mts', 'cts', 'js', 'mjs', 'cjs', 'tsx'],
  import: 'tsx',
  exit: true,
  karmaMochaConfig,
  ...karmaMochaConfig,
};
