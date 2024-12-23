import sinonChai from 'sinon-chai';
import { describe, it, expect, chai } from 'vitest';
import BackbonePlugin from '../../src/index.js';
import { createGenerator, loadInput, pathBase } from '../utils/common.js';

chai.use(sinonChai);

describe('BackbonePlugin', () => {
  describe('when there is a complex hierarchy of entities', () => {
    const sectionName = 'ComplexHierarchy';
    const modelSectionPath = `${pathBase}/${sectionName.toLowerCase()}/models`;

    it('correctly generates code', async () => {
      const generator = createGenerator([BackbonePlugin]);
      const input = await loadInput(sectionName, import.meta.url);
      const files = await generator.process(input);
      expect(files.length).to.equal(4);

      const [endpointFile, grandParentModelEntityFile, modelEntityFile, parentModelEntityFile] = files;
      await expect(await endpointFile.text()).toMatchFileSnapshot(`./fixtures/${sectionName}Endpoint.snap.ts`);
      await expect(await modelEntityFile.text()).toMatchFileSnapshot('./fixtures/Model.entity.snap.ts');
      await expect(await parentModelEntityFile.text()).toMatchFileSnapshot('./fixtures/ParentModel.entity.snap.ts');
      await expect(await grandParentModelEntityFile.text()).toMatchFileSnapshot(
        './fixtures/GrandParentModel.entity.snap.ts',
      );
      expect(endpointFile.name).to.equal(`${sectionName}Endpoint.ts`);
      expect(modelEntityFile.name).to.equal(`${modelSectionPath}/ComplexHierarchyModel.ts`);
      expect(parentModelEntityFile.name).to.equal(`${modelSectionPath}/ComplexHierarchyParentModel.ts`);
      expect(grandParentModelEntityFile.name).to.equal(`${modelSectionPath}/ComplexHierarchyGrandParentModel.ts`);
    });
  });
});
