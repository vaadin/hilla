const micromatch = require('micromatch');

function exclude(files) {
  const matched = micromatch.not(files, ['node_modules/**/*', 'packages/java/**/*', '**/*.snap.{ts,js}']);

  return matched.length > 0 ? [`prettier --write "${matched.join('" "')}"`] : [];
}

module.exports = {
  'packages/ts/**/src/**/*.{js,ts}': ['eslint --fix'],
  'packages/ts/**/test/**/*.{js,ts}': exclude,
  '*.{js,ts,json}': exclude,
};
