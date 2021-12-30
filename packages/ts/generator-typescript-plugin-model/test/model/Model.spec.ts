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
      const modelNames = [
        'FormEntityModel',
        'FormArrayTypesModel',
        'FormEntityHierarchyModel',
        'FormEntityIdModel',
        'FormValidatorsModel',
        'FormDataPrimitivesModel',
        'FormTemporalTypesModel',
        'FormRecordTypesModel',
        'FormOptionalTypesModel',
        'FormNonnullTypesModel',
      ];

      const generator = new Generator([ModelPlugin], createLogger({ name: 'model-plugin-test', verbose: true }));
      const input = await readFile(new URL('./Model.json', import.meta.url), 'utf8');
      const files = await generator.process(input);
      expect(files.length).to.equal(modelNames.length);

      const filesByName = files.reduce<Record<string, typeof files[0]>>((r, file) => {
        r[file.name] = file;
        return r;
      }, {});
      await Promise.all(
        modelNames.map(async (modelName) => {
          const fileName = `com/example/application/endpoints/TsFormEndpoint/${modelName}.ts`;
          const modelFile = filesByName[fileName];
          expect(modelFile).to.not.be.undefined;
          await expect(await modelFile.text()).toMatchSnapshot(modelName, import.meta.url);
        }),
      );
    });
  });
});
