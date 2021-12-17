import Generator from '@vaadin/generator-typescript-core/Generator.js';
import createLogger from '@vaadin/generator-typescript-utils/createLogger.js';
import snapshotMatcher from '@vaadin/generator-typescript-utils/testing/snapshotMatcher.js';
import { expect, use } from 'chai';
import { readFile } from 'fs/promises';
import sinonChai from 'sinon-chai';
import ModelPlugin from '../../src/index.js';

use(sinonChai);
use(snapshotMatcher);

describe('FormPlugin', () => {
  context('models', () => {
    it('correctly generates code', async () => {
      const generator = new Generator([ModelPlugin], createLogger({ name: 'model-plugin-test', verbose: true }));
      const input = await readFile(new URL('./Model.json', import.meta.url), 'utf8');
      const files = await generator.process(input);
      expect(files.length).to.equal(10);

      const filesByName = files.reduce((r, file) => {
        r[file.name] = file;
        return r;
      }, {} as Record<string, typeof files[0]>);
      [
        'FormArrayTypesModel',
        'FormDataPrimitivesModel',
        'FormEntityHierarchyModel',
        'FormEntityIdModel',
        'FormEntityModel',
        'FormNonnullTypesModel',
        'FormOptionalTypesModel',
        'FormRecordTypesModel',
        'FormTemporalTypesModel',
        'FormValidatorsModel',
      ].forEach(async (modelName) => {
        const modelFile = filesByName[modelName];
        expect(modelFile).to.not.be.undefined;
        expect(modelFile.name).to.equal(`./${modelName}.ts`);
        await expect(modelFile.text()).toMatchSnapshot(modelName, import.meta.url);
      });
    });
  });
});
