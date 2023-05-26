import micromatch from 'micromatch';

const excludePatterns = ['**/node_modules/**/*', '**/*.snap.{ts,js}'];

function createExcludeCallback(command) {
  return (files) => {
    const matched = micromatch.not(files, excludePatterns);

    return matched.length > 0 ? [`${command} ${matched.join(' ')}`] : [];
  };
}

export const commands = [createExcludeCallback('eslint --fix'), createExcludeCallback('prettier --write')];

export default {
  './*{.js,.ts}': commands,
};
