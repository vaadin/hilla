import ExportManager from './ExportManager.js';
import ImportManager from './ImportManager.js';
import NameRegistry from './NameRegistry.js';
import type PathManager from './PathManager.js';

export default class DependencyManager {
  readonly exports: ExportManager;
  readonly imports: ImportManager;
  readonly names: NameRegistry;
  readonly paths: PathManager;

  constructor(paths: PathManager, collator: Intl.Collator = new Intl.Collator('en', { sensitivity: 'case' })) {
    this.names = new NameRegistry();
    this.exports = new ExportManager(collator, this.names);
    this.imports = new ImportManager(collator, this.names);
    this.paths = paths;
  }
}
