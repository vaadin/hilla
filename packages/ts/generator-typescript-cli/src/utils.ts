import Pino from 'pino';
import type GeneratorIO from './GeneratorIO.js';

export type LoggerOptions = Readonly<{
  verbose?: boolean;
}>;

export function createLogger({ verbose }: LoggerOptions) {
  return Pino({
    name: 'tsgen',
    level: verbose ? 'debug' : 'info',
    transport: {
      target: 'pino-pretty',
      options: {
        ignore: 'time,hostname,pid',
      },
    },
  });
}

export async function processInput(raw: string, io: GeneratorIO): Promise<string> {
  let result = raw;

  if (result.startsWith("'") || result.startsWith('"')) {
    result = raw.substring(1, raw.length - 1);
  }

  if (result.startsWith('{')) {
    return result;
  }

  return io.read(io.resolve(result));
}
