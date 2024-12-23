import sinonChai from 'sinon-chai';
import { describe, it, expect, chai } from 'vitest';
import BackbonePlugin from '../../src/index.js';
import { createGenerator, loadInput } from '../utils/common.js';

chai.use(sinonChai);

describe('BackbonePlugin', () => {
  describe('when an entity with no properties is processed', () => {
    const sectionName = 'NoProperties';

    it('creates an empty TS interface without errors', async () => {
      const generator = createGenerator([BackbonePlugin]);
      const input = await loadInput(sectionName, import.meta.url);
      const files = await generator.process(input);
      expect(files.length).to.equal(3);

      const [endpointFile, entity1, entity2] = files;

      await expect(await endpointFile.text()).toMatchFileSnapshot(`./fixtures/${sectionName}Endpoint.snap.ts`);
      expect(endpointFile.name).to.equal(`${sectionName}Endpoint.ts`);

      await expect(await entity1.text()).toMatchFileSnapshot(`./fixtures/ExampleEntity.snap.ts`);
      expect(entity1.name).to.equal(`com/example/application/entities/ExampleEntity.ts`);

      await expect(await entity2.text()).toMatchFileSnapshot(`./fixtures/CoreEntity.snap.ts`);
      expect(entity2.name).to.equal(`com/example/application/entities/CoreEntity.ts`);
    });
  });
});
