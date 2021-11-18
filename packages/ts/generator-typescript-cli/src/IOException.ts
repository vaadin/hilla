export default class IOException extends Error {
  public constructor(message: string) {
    super(`[IOException]: ${message}`);
  }
}
