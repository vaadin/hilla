import BackbonePlugin from '@vaadin/hilla-generator-plugin-backbone';
import ModelPlugin from '@vaadin/hilla-generator-plugin-model';
import { describe, it, expect } from 'vitest';
import TransferTypesPlugin from '../../src/index.js';
import { createGenerator, loadInput } from '../utils/common.js';

describe('TransferTypesPlugin', () => {
  describe('for Spring Data Page, Slice, Pageable, Sort, Order types', () => {
    it('correctly replaces the incoming type', async () => {
      const sectionName = 'SpringPage';
      const generator = createGenerator([TransferTypesPlugin, BackbonePlugin, ModelPlugin]);
      const input = await loadInput(sectionName, import.meta.url);
      const files = await generator.process(input);

      expect(files).to.have.length(7);

      // Find the custom entity file
      const entityFile = files.find((f) => f.name.endsWith('/Custom.ts'));
      expect(entityFile).to.exist;

      await expect(await entityFile!.text()).toMatchFileSnapshot('./fixtures/Custom.snap.ts');

      // Find the custom entity model file
      const entityModelFile = files.find((f) => f.name.endsWith('/CustomModel.ts'));
      expect(entityModelFile).to.exist;

      await expect(await entityModelFile!.text()).toMatchFileSnapshot('./fixtures/CustomModel.snap.ts');

      // Find the main endpoint file
      const endpointFile = files.find((f) => f.name === 'PageableEndpoint.ts');
      expect(endpointFile).to.exist;

      await expect(await endpointFile!.text()).toMatchFileSnapshot('./fixtures/PageableEndpoint.snap.ts');
    });
  });
});
