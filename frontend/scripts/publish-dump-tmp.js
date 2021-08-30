import { resolve } from 'path';
import { readFile } from 'fs/promises';

const cwd = process.cwd();
const lernaConfigFile = resolve(cwd, 'lerna.json');

const content = await readFile(lernaConfigFile, 'utf8');
// eslint-disable-next-line no-console
console.log(JSON.parse(content));
