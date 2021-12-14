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
      expect(modelFiles.length).to.equal(3);

      const [myEntityModelFile, myEntityIdModelFile, myBazModelFile] = modelFiles;

      expect(myEntityModelFile.name).to.equal('./MyEntityModel.ts');
      // await expect(myEntityModelFile.text()).toMatchSnapshot('MyEntityModel', import.meta.url);
      expect(myEntityIdModelFile.name).to.equal('./MyEntityIdModel.ts');
      // await expect(myEntityIdModelFile.text()).toMatchSnapshot('MyEntityIdModel', import.meta.url);
      expect(myBazModelFile.name).to.equal('./MyBazModel.ts');
      // await expect(myBazModelFile.text()).toMatchSnapshot('MyBazModel', import.meta.url);
    });
  });
});
