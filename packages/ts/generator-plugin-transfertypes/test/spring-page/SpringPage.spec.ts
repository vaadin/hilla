import BackbonePlugin from '@vaadin/hilla-generator-plugin-backbone';
import { describe, it, expect } from 'vitest';
import TransferTypesPlugin from '../../src/index.js';
import { createGenerator, loadInput } from '../utils/common.js';

describe('TransferTypesPlugin', () => {
  describe('for Spring Data Page, Slice, Pageable, Sort, Order types', () => {
    it('correctly replaces the incoming type', async () => {
      const sectionName = 'SpringPage';
      const generator = createGenerator([TransferTypesPlugin, BackbonePlugin]);
      const input = await loadInput(sectionName, import.meta.url);
      const files = await generator.process(input);

      // Expect 3 files: endpoint + enum files for Direction and NullHandling
      expect(files).to.have.length(3);

      // Find the main endpoint file
      const endpointFile = files.find((f) => f.name === 'PageableEndpoint.ts');
      expect(endpointFile).to.exist;

      await expect(await endpointFile!.text()).toMatchFileSnapshot('./fixtures/PageableEndpoint.snap.ts');
    });
  });
});
