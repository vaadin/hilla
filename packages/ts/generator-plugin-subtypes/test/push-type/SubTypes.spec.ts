/* eslint-disable import/no-extraneous-dependencies */
import BackbonePlugin from '@vaadin/hilla-generator-plugin-backbone/index.js';
import ModelPlugin from '@vaadin/hilla-generator-plugin-model/index.js';
import sinonChai from 'sinon-chai';
import { chai, describe, expect, it } from 'vitest';
import SubTypesPlugin from '../../src/index.js';
import { createGenerator, loadInput } from '../utils/common.js';

chai.use(sinonChai);

describe('SubTypesPlugin', () => {
  describe('when the entity has `oneOf`', () => {
    it('generates as union type', async () => {
      const sectionName = 'SubTypes';
      const generator = createGenerator([BackbonePlugin, ModelPlugin, SubTypesPlugin]);
      const input = await loadInput(sectionName, import.meta.url);
      const files = await generator.process(input);
      expect(files.length).to.equal(10);

      const t = await files[1].text();
      expect(t).to.exist;

      const endpointFile = files.find((f) => f.name === 'SubTypesEndpoint.ts')!;
      expect(endpointFile).to.exist;
      await expect(await endpointFile.text()).toMatchFileSnapshot(`fixtures/${sectionName}Endpoint.snap.ts`);
      expect(endpointFile.name).to.equal(`${sectionName}Endpoint.ts`);

      const baseEventUnionFile = files.find(
        (f) => f.name === 'com/vaadin/hilla/parser/plugins/subtypes/BaseEventUnion.ts',
      )!;
      expect(baseEventUnionFile).to.exist;
      await expect(await baseEventUnionFile.text()).toMatchFileSnapshot('fixtures/BaseEventUnion.snap.ts');
      expect(baseEventUnionFile.name).to.equal('com/vaadin/hilla/parser/plugins/subtypes/BaseEventUnion.ts');

      const baseEventFile = files.find((f) => f.name === 'com/vaadin/hilla/parser/plugins/subtypes/BaseEvent.ts')!;
      expect(baseEventFile).to.exist;
      await expect(await baseEventFile.text()).toMatchFileSnapshot('fixtures/BaseEvent.snap.ts');
      expect(baseEventFile.name).to.equal('com/vaadin/hilla/parser/plugins/subtypes/BaseEvent.ts');

      const addEventFile = files.find((f) => f.name === 'com/vaadin/hilla/parser/plugins/subtypes/AddEvent.ts')!;
      expect(addEventFile).to.exist;
      await expect(await addEventFile.text()).toMatchFileSnapshot('fixtures/AddEvent.snap.ts');
      expect(addEventFile.name).to.equal('com/vaadin/hilla/parser/plugins/subtypes/AddEvent.ts');

      const addEventModelFile = files.find(
        (f) => f.name === 'com/vaadin/hilla/parser/plugins/subtypes/AddEventModel.ts',
      )!;
      expect(addEventModelFile).to.exist;
      await expect(await addEventModelFile.text()).toMatchFileSnapshot('fixtures/AddEventModel.snap.ts');
      expect(addEventModelFile.name).to.equal('com/vaadin/hilla/parser/plugins/subtypes/AddEventModel.ts');
    });
  });
});
