import Pino, { type Logger } from 'pino';
import PinoPretty from 'pino-pretty';

export { type Logger };

export type LoggerOptions = Readonly<{
  name?: string;
  verbose?: boolean;
}>;

export default class LoggerFactory {
  readonly #children = new Map<string, Logger>();
  readonly #logger: Logger;

  constructor({ name, verbose }: LoggerOptions) {
    const pretty = PinoPretty({
      ignore: 'time',
    });

    this.#logger = Pino(
      {
        base: undefined,
        level: verbose ? 'debug' : 'info',
        name: name ?? 'tsgen',
      },
      pretty,
    );
  }

  get global(): Logger {
    return this.#logger;
  }

  for(caller: string): Logger {
    if (this.#children.has(caller)) {
      return this.#children.get(caller)!;
    }

    const child = this.#logger.child({ caller });
    this.#children.set(caller, child);
    return child;
  }
}
