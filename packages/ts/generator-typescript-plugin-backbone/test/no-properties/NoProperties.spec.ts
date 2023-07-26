/* eslint-disable import/no-extraneous-dependencies */
import snapshotMatcher from '@hilla/generator-typescript-utils/testing/snapshotMatcher.js';
import { expect, use } from 'chai';
import sinonChai from 'sinon-chai';
import BackbonePlugin from '../../src/index.js';
import { createGenerator, loadInput } from '../utils/common.js';

use(sinonChai);
use(snapshotMatcher);

describe('BackbonePlugin', () => {
  context('when an entity with no properties is processed', () => {
    const sectionName = 'NoProperties';

    it('creates an empty TS interface without errors', async () => {
      const generator = createGenerator([BackbonePlugin]);
      const input = await loadInput(sectionName, import.meta.url);
      const files = await generator.process(input);
      expect(files.length).to.equal(3);

      const [endpointFile, entity1, entity2] = files;

      await expect(await endpointFile.text()).toMatchSnapshot(`${sectionName}Endpoint`, import.meta.url);
      expect(endpointFile.name).to.equal(`${sectionName}Endpoint.ts`);

      await expect(await entity1.text()).toMatchSnapshot(`ExampleEntity`, import.meta.url);
      expect(entity1.name).to.equal(`com/example/application/entities/ExampleEntity.ts`);

      await expect(await entity2.text()).toMatchSnapshot(`CoreEntity`, import.meta.url);
      expect(entity2.name).to.equal(`com/example/application/entities/CoreEntity.ts`);
    });
  });
});
