import { join } from 'node:path';
import ClientPlugin from '@vaadin/hilla-generator-plugin-client';
import sinonChai from 'sinon-chai';
import { describe, it, expect, chai } from 'vitest';
import BackbonePlugin from '../../src/index.js';
import { createGenerator, loadInput } from '../utils/common.js';

chai.use(sinonChai);

describe('BackbonePlugin', () => {
  describe('when a custom client file is available', () => {
    const sectionName = 'CustomClient';

    it('correctly generates code', async () => {
      const generator = createGenerator([BackbonePlugin, ClientPlugin], join(import.meta.url, '../fixtures'));
      const input = await loadInput(sectionName, import.meta.url);
      const files = await generator.process(input);
      expect(files.length).to.equal(1);

      const [endpointFile] = files;
      await expect(await endpointFile.text()).toMatchFileSnapshot(`./fixtures/${sectionName}Endpoint.snap.ts`);
      expect(endpointFile.name).to.equal(`${sectionName}Endpoint.ts`);
    });
  });
});
