import micromatch from 'micromatch';

const excludePatterns = ['**/node_modules/**/*', '**/*.snap.{ts,js}'];

function createExcludeCallback(command) {
  return (files) => {
    const matched = micromatch.not(files, excludePatterns);

    return matched.length > 0 ? [`${command} ${matched.map((filename) => `"${filename}"`).join(' ')}`] : [];
  };
}

export const extensions = ['ts', 'js', 'tsx'].join(',');
export const commands = [createExcludeCallback('eslint --fix'), createExcludeCallback('prettier --write')];

export default {
  [`./*.{${extensions}}`]: commands,
};
