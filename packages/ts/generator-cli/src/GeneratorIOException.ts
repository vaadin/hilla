export default class GeneratorIOException extends Error {
  constructor(message: string) {
    super(`[GeneratorIOException]: ${message}`);
  }
}
