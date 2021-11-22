export default class GeneratorError extends Error {
  public constructor(message: string) {
    super(`[GeneratorError]: ${message}`);
  }
}
