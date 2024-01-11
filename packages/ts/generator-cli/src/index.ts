import Generator from '@vaadin/hilla-generator-core/Generator.js';
import LoggerFactory from '@vaadin/hilla-generator-utils/LoggerFactory.js';
import meow from 'meow';
import GeneratorIO from './GeneratorIO.js';
import { processInput } from './utils.js';

const {
  flags: { outputDir, plugin: plugins, verbose },
  input: [input],
} = meow(
  `
Usage:
  tsgen
    (will read JSON from stdin)
  tsgen <OpenAPI JSON string>
  tsgen <OpenAPI file path>

Options:
  -h, --help             Show this screen
  -o, --output-dir       Output directory
  -p, --plugin <path>    Use the plugin loadable by <path>.
  --version              Show the app version
`,
  {
    flags: {
      outputDir: {
        default: 'frontend/generated',
        shortFlag: 'o',
        type: 'string',
      },
      plugin: {
        default: [],
        isMultiple: true,
        shortFlag: 'p',
        type: 'string',
      },
      verbose: {
        shortFlag: 'v',
        type: 'boolean',
      },
    },
    importMeta: import.meta,
  },
);

const logger = new LoggerFactory({ verbose });

const io = new GeneratorIO(outputDir, logger);

const resolvedPlugins = await Promise.all(
  Array.from(new Set(plugins), async (pluginPath) => io.loadPlugin(pluginPath)),
);
const generator = new Generator(resolvedPlugins, { logger, outputDir });

const files = await generator.process(await processInput(io, input));
const filesToDelete = await io.getGeneratedFiles();
const generatedFiles = await io.writeGeneratedFiles(files);

await io.cleanOutputDir(generatedFiles, filesToDelete);
