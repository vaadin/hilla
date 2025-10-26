/* eslint-disable import/no-extraneous-dependencies */
import sinonChai from 'sinon-chai';
import { chai, describe, expect, it } from 'vitest';
import BackbonePlugin from '../../src/index.js';
import { createGenerator, loadInput } from '../utils/common.js';

chai.use(sinonChai);

describe('BackbonePlugin', () => {
  describe('when bare generic types are used', () => {
    const sectionName = 'BareGenerics';

    it('correctly generates code without crashing', async () => {
      const generator = createGenerator([BackbonePlugin]);
      const input = await loadInput(sectionName, import.meta.url);
      const files = await generator.process(input);
      expect(files.length).to.equal(1);

      const [endpointFile] = files;
      await expect(await endpointFile.text()).toMatchFileSnapshot(`fixtures/${sectionName}Endpoint.snap.ts`);
      expect(endpointFile.name).to.equal(`${sectionName}Endpoint.ts`);
    });
  });
});
