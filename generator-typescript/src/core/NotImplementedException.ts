export default class NotImplementedException extends Error {
  public constructor(cls: string, member: string) {
    super(`Member '${member}' is not implemented for class '${cls}'`);
  }
}
