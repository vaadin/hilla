/* eslint-disable import/no-extraneous-dependencies */
import BackbonePlugin from '@vaadin/hilla-generator-plugin-backbone/index.js';
import ModelPlugin from '@vaadin/hilla-generator-plugin-model/index.js';
import snapshotMatcher from '@vaadin/hilla-generator-utils/testing/snapshotMatcher.js';
import { expect, use } from 'chai';
import sinonChai from 'sinon-chai';
import SubTypesPlugin from '../../src/index.js';
import { createGenerator, loadInput } from '../utils/common.js';

use(sinonChai);
use(snapshotMatcher);

describe('SubTypesPlugin', () => {
  context('when the entity has `oneOf`', () => {
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
      await expect(await endpointFile.text()).toMatchSnapshot(`${sectionName}Endpoint.snap.ts`, import.meta.url);
      expect(endpointFile.name).to.equal(`${sectionName}Endpoint.ts`);

      const baseEventUnionFile = files.find(
        (f) => f.name === 'com/vaadin/hilla/parser/plugins/subtypes/BaseEventUnion.ts',
      )!;
      expect(baseEventUnionFile).to.exist;
      await expect(await baseEventUnionFile.text()).toMatchSnapshot('BaseEventUnion.snap.ts', import.meta.url);
      expect(baseEventUnionFile.name).to.equal('com/vaadin/hilla/parser/plugins/subtypes/BaseEventUnion.ts');

      const baseEventFile = files.find((f) => f.name === 'com/vaadin/hilla/parser/plugins/subtypes/BaseEvent.ts')!;
      expect(baseEventFile).to.exist;
      await expect(await baseEventFile.text()).toMatchSnapshot('BaseEvent.snap.ts', import.meta.url);
      expect(baseEventFile.name).to.equal('com/vaadin/hilla/parser/plugins/subtypes/BaseEvent.ts');

      const addEventFile = files.find((f) => f.name === 'com/vaadin/hilla/parser/plugins/subtypes/AddEvent.ts')!;
      expect(addEventFile).to.exist;
      await expect(await addEventFile.text()).toMatchSnapshot('AddEvent.snap.ts', import.meta.url);
      expect(addEventFile.name).to.equal('com/vaadin/hilla/parser/plugins/subtypes/AddEvent.ts');

      const addEventModelFile = files.find(
        (f) => f.name === 'com/vaadin/hilla/parser/plugins/subtypes/AddEventModel.ts',
      )!;
      expect(addEventModelFile).to.exist;
      await expect(await addEventModelFile.text()).toMatchSnapshot('AddEventModel.snap.ts', import.meta.url);
      expect(addEventModelFile.name).to.equal('com/vaadin/hilla/parser/plugins/subtypes/AddEventModel.ts');
    });
  });
});
