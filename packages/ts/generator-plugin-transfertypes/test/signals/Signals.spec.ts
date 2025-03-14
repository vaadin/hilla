import BackbonePlugin from '@vaadin/hilla-generator-plugin-backbone';
import { describe, it, expect } from 'vitest';
import TransferTypesPlugin from '../../src/index.js';
import { createGenerator, loadInput } from '../utils/common.js';

describe('TransferTypesPlugin', () => {
  describe('for Signal, NumberSignal, ValueSignal, ListSignal types', () => {
    it('correctly replaces the incoming type', async () => {
      const sectionName = 'Signals';
      const generator = createGenerator([TransferTypesPlugin, BackbonePlugin]);
      const input = await loadInput(sectionName, import.meta.url);
      const files = await generator.process(input);
      expect(files).to.have.length(1);
      expect(files[0].name).to.equal('SignalEndpoint.ts');
      await expect(await files[0].text()).toMatchFileSnapshot('./fixtures/SignalEndpoint.snap.ts');
    });
  });
});
