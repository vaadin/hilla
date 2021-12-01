import ExportManager from './ExportManager.js';
import ImportManager from './ImportManager.js';

export default class DependencyManager {
  public readonly exports: ExportManager;
  public readonly imports: ImportManager;

  public constructor(collator: Intl.Collator = new Intl.Collator('en', { sensitivity: 'case' })) {
    this.exports = new ExportManager(collator);
    this.imports = new ImportManager(collator);
  }
}
