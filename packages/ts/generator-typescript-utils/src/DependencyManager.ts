import ExportManager from './ExportManager.js';
import ImportManager from './ImportManager.js';
import type PathManager from './PathManager.js';

export default class DependencyManager {
  public readonly exports: ExportManager;
  public readonly imports: ImportManager;
  public readonly paths: PathManager;

  public constructor(paths: PathManager, collator: Intl.Collator = new Intl.Collator('en', { sensitivity: 'case' })) {
    this.exports = new ExportManager(collator);
    this.imports = new ImportManager(collator);
    this.paths = paths;
  }
}
