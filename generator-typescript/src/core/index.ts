import meow from 'meow';
import Generator from './Generator';

const {
  input: [input],
  flags: { config, verbose },
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
      verbose: {
        alias: 'v',
        type: 'boolean',
      },
    },
  }
);

const generator = await Generator.init(config, { verbose });
generator.process(input);
