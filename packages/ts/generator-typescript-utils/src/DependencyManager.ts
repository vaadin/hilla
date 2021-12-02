import ExportManager from './ExportManager.js';
import ImportManager from './ImportManager.js';
import type PathProcessor from './PathProcessor.js';

export default class DependencyManager {
  public readonly exports: ExportManager;
  public readonly imports: ImportManager;

  public constructor(path: PathProcessor, collator: Intl.Collator = new Intl.Collator('en', { sensitivity: 'case' })) {
    this.exports = new ExportManager(path, collator);
    this.imports = new ImportManager(path, collator);
  }
}
