import Generator from '@vaadin/generator-typescript-core/Generator.js';
import meow from 'meow';
import IO from './IO.js';
import { createLogger, processInput } from './utils.js';

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

const logger = createLogger({ verbose });

const io = new IO(outputDir, logger);

const generator = new Generator(await io.load(Array.from(new Set(plugins), (path) => io.resolve(path))), logger);

const files = await generator.process(await processInput(input, io));
await io.write(files);
