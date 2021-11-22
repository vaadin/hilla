import type Pino from 'pino';
import type { Constructor } from 'type-fest';
import type ReferenceResolver from './ReferenceResolver.js';
import type SharedStorage from './SharedStorage.js';

export default abstract class Plugin {
  protected readonly logger: Pino.Logger;
  protected readonly resolver: ReferenceResolver;

  public constructor(resolver: ReferenceResolver, logger: Pino.Logger) {
    this.logger = logger;
    this.resolver = resolver;
  }

  public get name(): string {
    return this.constructor.name;
  }

  public abstract get path(): string;

  public abstract execute(storage: SharedStorage): Promise<void>;
}

export type PluginConstructor = Constructor<Plugin, ConstructorParameters<typeof Plugin>>;
