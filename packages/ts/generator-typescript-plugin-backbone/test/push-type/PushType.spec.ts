/* eslint-disable import/no-extraneous-dependencies */
import snapshotMatcher from '@hilla/generator-typescript-utils/testing/snapshotMatcher.js';
import { expect, use } from 'chai';
import sinonChai from 'sinon-chai';
import BackbonePlugin from '../../src/index.js';
import { createGenerator, loadInput, pathBase } from '../utils/common.js';

use(sinonChai);
use(snapshotMatcher);

describe('BackbonePlugin', () => {
  context('when the endpoint method has a return type related to push support', () => {
    const sectionName = 'PushType';
    const modelSectionPath = `${pathBase}/${sectionName.toLowerCase()}/${sectionName}Endpoint`;

    it('correctly replaces types', async () => {
      const generator = createGenerator([BackbonePlugin]);
      const input = await loadInput(sectionName, import.meta.url);
      const files = await generator.process(input);
      expect(files.length).to.equal(2);

      const t = await files[1].text();
      expect(t).to.exist;

      const [endpointFile, userEntity] = files;
      await expect(await endpointFile.text()).toMatchSnapshot(`${sectionName}Endpoint`, import.meta.url);
      await expect(await userEntity.text()).toMatchSnapshot('UserEntity.entity', import.meta.url);
      expect(endpointFile.name).to.equal(`${sectionName}Endpoint.ts`);
      expect(userEntity.name).to.equal(`${modelSectionPath}/UserEntity.ts`);
    });
  });
});
