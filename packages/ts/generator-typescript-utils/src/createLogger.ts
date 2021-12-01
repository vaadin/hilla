import Pino from 'pino';

export type LoggerOptions = Readonly<{
  name?: string;
  verbose?: boolean;
}>;

export default function createLogger({ name = 'tsgen', verbose }: LoggerOptions): Pino.Logger {
  return Pino({
    name,
    level: verbose ? 'debug' : 'info',
    transport: {
      target: 'pino-pretty',
      options: {
        ignore: 'time,hostname,pid',
      },
    },
  });
}
