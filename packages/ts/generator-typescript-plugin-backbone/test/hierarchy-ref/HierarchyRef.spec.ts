/* eslint-disable import/no-extraneous-dependencies */
import snapshotMatcher from '@hilla/generator-typescript-utils/testing/snapshotMatcher.js';
import { expect, use } from 'chai';
import sinonChai from 'sinon-chai';
import BackbonePlugin from '../../src/index.js';
import { createGenerator, loadInput, pathBase } from '../utils/common.js';

use(sinonChai);
use(snapshotMatcher);

describe('BackbonePlugin', () => {
  context('when there is an entity referring its superclass', () => {
    const sectionName = 'HierarchyRef';
    const modelSectionPath = `${pathBase}/${sectionName.toLowerCase()}/${sectionName}Endpoint`;

    it.only('correctly generates code', async () => {
      const generator = createGenerator([BackbonePlugin]);
      const input = await loadInput(sectionName, import.meta.url);
      const files = await generator.process(input);
      expect(files.length).to.equal(3);

      const [endpointFile, entity] = files;
      await expect(await endpointFile.text()).toMatchSnapshot(`${sectionName}Endpoint`, import.meta.url);
      await expect(await entity.text()).toMatchSnapshot('HierarchyRef.entity', import.meta.url);
      expect(endpointFile.name).to.equal(`${sectionName}Endpoint.ts`);
      expect(entity.name).to.equal(`${modelSectionPath}/HierarchyRef.ts`);
    });
  });
});
