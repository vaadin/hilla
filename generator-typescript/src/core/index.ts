import { resolve } from 'path';
import { readFile } from 'fs/promises';
import meow from 'meow';
import type { GeneratorConfig } from './Generator';
import Generator from './Generator';

const {
  input: [input],
  flags: { config },
} = meow(
  `
  Usage:
    tsgen <OpenAPI JSON string>
    tsgen <OpenAPI file path>

  Options:
    -h, --help           Show this screen
    --version            Show the app version
    -c, --config <path>  Path to a config file
`,
  {
    importMeta: import.meta,
    flags: {
      config: {
        alias: 'c',
        type: 'string',
        isRequired: true,
      },
    },
  }
);

const generatorConfig: GeneratorConfig = JSON.parse(await readFile(resolve(process.cwd(), config), 'utf8'));
await new Generator(generatorConfig).process(input);
