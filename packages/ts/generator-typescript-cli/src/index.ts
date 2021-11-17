import meow from 'meow';
import createApplication from './createApplication';

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

await createApplication(input, { outputDir, plugins, verbose });
