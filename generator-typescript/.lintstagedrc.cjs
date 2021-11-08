const micromatch = require('micromatch');
module.exports = {
  'src/**/*.{js,ts}': ['eslint --fix'],
  'tests/**/*.{js,ts}': (files) => {
    const matched = micromatch.not('tests/**/*.spec.{js,ts}', files);
    return [`eslint --fix ${matched.join(' ')}`];
  },
  '*.{js,ts,json}': ['prettier --write'],
};
