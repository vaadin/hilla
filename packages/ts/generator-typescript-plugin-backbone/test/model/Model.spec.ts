/* eslint-disable import/no-extraneous-dependencies */
import snapshotMatcher from '@vaadin/generator-typescript-utils/testing/snapshotMatcher.js';
import { expect, use } from 'chai';
import sinonChai from 'sinon-chai';
import BackbonePlugin from '../../src/index.js';
import { createGenerator, loadInput } from '../utils/common.js';

use(sinonChai);
use(snapshotMatcher);

describe('FormPlugin', () => {
  context('models', () => {
    it('correctly generates code', async () => {
      const generator = createGenerator([BackbonePlugin]);
      const input = await loadInput('Model', import.meta.url);
      const files = await generator.process(input);
      const modelFiles = files.filter((file) => file.name.endsWith('Model.ts'));
      expect(modelFiles.length).to.equal(10);

      const filesByName = modelFiles.reduce((r, file) => {
        r[file.name] = file;
        return r;
      }, {} as Record<string, typeof modelFiles[0]>);
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
