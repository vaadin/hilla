/* eslint-disable import/no-extraneous-dependencies */
import { resolve } from 'path';
import { readFile, writeFile } from 'fs/promises';
import meow from 'meow';

const {
  flags: { version },
} = meow({
  importMeta: import.meta,
  flags: {
    version: {
      type: 'string',
      alias: 'v',
    },
  },
});

const cwd = process.cwd();
const lernaConfigFile = resolve(cwd, 'lerna.json');

const file = await readFile(lernaConfigFile, 'utf8');

const config = {
  ...JSON.parse(file),
  version,
};

await writeFile(lernaConfigFile, JSON.stringify(config, null, 2), 'utf8');
