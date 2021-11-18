import Generator from '@vaadin/generator-typescript/Generator.js';
import meow from 'meow';
import Pino from 'pino';
import IO from './IO.js';

const {
  input: [input],
  flags: { outputDir, plugin: plugins, verbose },
} = meow(
  `
  Usage:
    tsgen <OpenAPI JSON string>
    tsgen <OpenAPI file path>

  Options:
    -h, --help             Show this screen
    -o, --output-dir       Output directory
    -p, --plugin <path>    Use the plugin loadable by <path>.
    --version              Show the app version
`,
  {
    importMeta: import.meta,
    flags: {
      outputDir: {
        alias: 'o',
        default: 'frontend/generated',
        type: 'string',
      },
      plugin: {
        alias: 'p',
        default: [],
        isMultiple: true,
        type: 'string',
      },
      verbose: {
        alias: 'v',
        type: 'boolean',
      },
    },
  },
);

const io = new IO(outputDir);

const generator = new Generator(
  await io.load(Array.from(new Set(plugins), (path) => io.resolve(path))),
  Pino({
    name: 'tsgen',
    level: verbose ? 'debug' : 'info',
  }),
);

const files = await generator.process(input.startsWith('{') ? input : await io.read(input));
await io.write(files);
