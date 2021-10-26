import type Pino from 'pino';
import type { Constructor } from 'type-fest';
import type SharedStorage from './SharedStorage';

export default abstract class Plugin {
  protected readonly logger: Pino.Logger;

  public constructor(logger: Pino.Logger) {
    this.logger = logger;
  }

  public get name(): string {
    return this.constructor.name;
  }

  public abstract get path(): string;

  public abstract execute(storage: SharedStorage): Promise<void>;
}

export type PluginConstructor = Constructor<Plugin, ConstructorParameters<typeof Plugin>>;
