import meow from 'meow';
import Application from './Application.js';

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
  },
);

await new Application({ verbose }).execute(config, input);
