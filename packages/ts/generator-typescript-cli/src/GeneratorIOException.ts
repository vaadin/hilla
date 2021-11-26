export default class GeneratorIOException extends Error {
  public constructor(message: string) {
    super(`[GeneratorIOException]: ${message}`);
  }
}
