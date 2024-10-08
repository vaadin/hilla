/* eslint-disable import/no-extraneous-dependencies */
import snapshotMatcher from '@vaadin/hilla-generator-utils/testing/snapshotMatcher.js';
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

    it('correctly generates code', async () => {
      const generator = createGenerator([BackbonePlugin]);
      const input = await loadInput(sectionName, import.meta.url);
      const files = await generator.process(input);
      expect(files.length).to.equal(3);

      const [endpointFile, entity] = files;
      await expect(await endpointFile.text()).toMatchSnapshot(`${sectionName}Endpoint.snap.ts`, import.meta.url);
      await expect(await entity.text()).toMatchSnapshot('HierarchyRef.entity.snap.ts', import.meta.url);
      expect(endpointFile.name).to.equal(`${sectionName}Endpoint.ts`);
      expect(entity.name).to.equal(`${modelSectionPath}/HierarchyRef.ts`);
    });
  });
});
