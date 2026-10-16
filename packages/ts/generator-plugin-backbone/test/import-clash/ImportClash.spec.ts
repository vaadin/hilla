/* eslint-disable import/no-extraneous-dependencies */
import ClientPlugin from '@vaadin/hilla-generator-plugin-client';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { afterEach, chai, describe, expect, it } from 'vitest';
import BackbonePlugin from '../../src/index.js';
import { createGenerator, loadInput } from '../utils/common.js';

chai.use(sinonChai);

describe('BackbonePlugin', () => {
  describe('when an endpoint method parameter has the name of an import', () => {
    const sectionName = 'ImportClash';

    it('suffixes the import instead of the parameter or the method', async () => {
      const generator = createGenerator([BackbonePlugin]);
      const input = await loadInput(sectionName, import.meta.url);
      const files = await generator.process(input);

      const [endpointFile] = files;
      await expect(await endpointFile.text()).toMatchFileSnapshot(`fixtures/${sectionName}Endpoint.snap.ts`);
      expect(endpointFile.name).to.equal(`${sectionName}Endpoint.ts`);
    });

    it('suffixes the import instead of the type parameter', async () => {
      const generator = createGenerator([BackbonePlugin]);
      const input = await loadInput(sectionName, import.meta.url);
      const files = await generator.process(input);

      const containerFile = files.find((f) => f.name.endsWith('/Container.ts'))!;
      await expect(await containerFile.text()).toMatchFileSnapshot('fixtures/Container.entity.snap.ts');
    });
  });

  describe('when two methods import types of the same name', () => {
    afterEach(() => {
      sinon.restore();
    });

    it('gives the plain name to the import of the first method, whichever finishes first', async () => {
      // every lookup of the client file waits less than the one before, so a
      // later method gets past it first
      const getClientFileName = ClientPlugin.getClientFileName.bind(ClientPlugin);
      let delay = 50;
      sinon.stub(ClientPlugin, 'getClientFileName').callsFake(async (path) => {
        delay -= 10;
        await new Promise((resolve) => {
          setTimeout(resolve, delay);
        });
        return getClientFileName(path);
      });

      const generator = createGenerator([BackbonePlugin]);
      const input = await loadInput('ImportOrder', import.meta.url);
      const [endpointFile] = await generator.process(input);
      const text = await endpointFile.text();

      expect(text).to.contain(
        'import type Collection from "./com/vaadin/hilla/parser/plugins/backbone/importorder/first/Collection.js";',
      );
      expect(text).to.contain(
        'import type Collection_1 from "./com/vaadin/hilla/parser/plugins/backbone/importorder/second/Collection.js";',
      );
    });
  });
});
