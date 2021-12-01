export default class PluginError extends Error {
  public constructor(message: string, pluginName = 'Unknown Generator Plugin') {
    super(`[${pluginName}]: ${message}`);
  }
}
