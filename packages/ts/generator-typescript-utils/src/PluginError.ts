export default class PluginError extends Error {
  constructor(message: string, pluginName = 'Unknown Generator Plugin') {
    super(`[${pluginName}]: ${message}`);
  }
}
