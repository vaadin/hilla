import { commands } from '../../../.lintstagedrc.js';

// TODO: Remove when project is updated to the new eslint rules.
const [, prettier] = commands;

export default {
  'src/**/*.{js,ts}': prettier,
  'test/**/*.{js,ts}': prettier,
};
