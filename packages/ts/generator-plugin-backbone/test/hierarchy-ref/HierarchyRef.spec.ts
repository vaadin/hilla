import sinonChai from 'sinon-chai';
import { describe, it, expect, chai } from 'vitest';
import BackbonePlugin from '../../src/index.js';
import { createGenerator, loadInput, pathBase } from '../utils/common.js';

chai.use(sinonChai);

describe('BackbonePlugin', () => {
  describe('when there is an entity referring its superclass', () => {
    const sectionName = 'HierarchyRef';
    const modelSectionPath = `${pathBase}/${sectionName.toLowerCase()}/${sectionName}Endpoint`;

    it('correctly generates code', async () => {
      const generator = createGenerator([BackbonePlugin]);
      const input = await loadInput(sectionName, import.meta.url);
      const files = await generator.process(input);
      expect(files.length).to.equal(3);

      const [endpointFile, entity] = files;
      await expect(await endpointFile.text()).toMatchFileSnapshot(`./fixtures/${sectionName}Endpoint.snap.ts`);
      await expect(await entity.text()).toMatchFileSnapshot('./fixtures/HierarchyRef.entity.snap.ts');
      expect(endpointFile.name).to.equal(`${sectionName}Endpoint.ts`);
      expect(entity.name).to.equal(`${modelSectionPath}/HierarchyRef.ts`);
    });
  });
});
