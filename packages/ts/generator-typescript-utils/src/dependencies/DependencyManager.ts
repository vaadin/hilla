import ExportManager from './ExportManager.js';
import ImportManager from './ImportManager.js';
import type PathManager from './PathManager.js';

export default class DependencyManager {
  readonly exports: ExportManager;
  readonly imports: ImportManager;
  readonly paths: PathManager;

  constructor(paths: PathManager, collator: Intl.Collator = new Intl.Collator('en', { sensitivity: 'case' })) {
    this.exports = new ExportManager(collator);
    this.imports = new ImportManager(collator);
    this.paths = paths;
  }
}
