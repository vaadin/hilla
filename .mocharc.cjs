const isCI = !!process.env.CI;

const karmaMochaConfig = {
  forbidOnly: isCI,
}

module.exports = {
  extensions: ['ts', 'mts', 'cts', 'js', 'mjs', 'cjs'],
  loader: 'tsx',
  exit: true,
  karmaMochaConfig,
  ...karmaMochaConfig,
};
