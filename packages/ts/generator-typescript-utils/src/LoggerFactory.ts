import Pino from 'pino';
import PinoPretty from 'pino-pretty';

export type Logger = Pino.Logger;

export type LoggerOptions = Readonly<{
  name?: string;
  verbose?: boolean;
}>;

export default class LoggerFactory {
  readonly #children = new Map<string, Pino.Logger>();
  readonly #logger: Pino.Logger;

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

  get global(): Pino.Logger {
    return this.#logger;
  }

  for(caller: string): Pino.Logger {
    if (this.#children.has(caller)) {
      return this.#children.get(caller)!;
    }

    const child = this.#logger.child({ caller });
    this.#children.set(caller, child);
    return child;
  }
}
