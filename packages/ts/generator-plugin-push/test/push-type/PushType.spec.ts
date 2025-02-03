/* eslint-disable import/no-extraneous-dependencies */
import BackbonePlugin from '@vaadin/hilla-generator-plugin-backbone/index.js';
import sinonChai from 'sinon-chai';
import { chai, describe, expect, it } from 'vitest';
import PushPlugin from '../../src/index.js';
import { createGenerator, loadInput } from '../utils/common.js';

chai.use(sinonChai);

describe('PushPlugin', () => {
  describe('when the endpoint method has a return type related to push support', () => {
    it('correctly replaces types', async () => {
      const sectionName = 'PushType';
      const generator = createGenerator([BackbonePlugin, PushPlugin]);
      const input = await loadInput(sectionName, import.meta.url);
      const files = await generator.process(input);
      expect(files.length).to.equal(2);

      const t = await files[1].text();
      expect(t).to.exist;

      const endpointFile = files.find((f) => f.name === 'PushTypeEndpoint.ts')!;
      expect(endpointFile).to.exist;
      await expect(await endpointFile.text()).toMatchFileSnapshot(`fixtures/${sectionName}Endpoint.snap.ts`);
      expect(endpointFile.name).to.equal(`${sectionName}Endpoint.ts`);
    });

    it('removes init import if not needed', async () => {
      const sectionName = 'PushTypeOnly';
      const generator = createGenerator([BackbonePlugin, PushPlugin]);
      const input = await loadInput(sectionName, import.meta.url);
      const files = await generator.process(input);
      expect(files.length).to.equal(2);

      const t = await files[1].text();
      expect(t).to.exist;

      const endpointFile = files.find((f) => f.name === 'PushTypeOnlyEndpoint.ts')!;
      expect(endpointFile).to.exist;
      await expect(await endpointFile.text()).toMatchFileSnapshot(`fixtures/${sectionName}Endpoint.snap.ts`);
      expect(endpointFile.name).to.equal(`${sectionName}Endpoint.ts`);
    });
  });
});
