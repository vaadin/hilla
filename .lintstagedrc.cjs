const micromatch = require('micromatch');

function exclude(files) {
  const matched = micromatch.not(files, ['{node_modules,java}/**/*', '**/*.snap.{ts,js}']);

  return [`prettier --write ${matched.join(' ')}`];
}

module.exports = {
  'ts/**/src/**/*.{js,ts}': ['eslint --fix'],
  'ts/**/test/**/*.{js,ts}': exclude,
  '*.{js,ts,json}': exclude,
};
