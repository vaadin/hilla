import { readFile } from 'node:fs/promises';
import Generator from '@vaadin/hilla-generator-core/Generator.js';
import LoggerFactory from '@vaadin/hilla-generator-utils/LoggerFactory.js';
import snapshotMatcher from '@vaadin/hilla-generator-utils/testing/snapshotMatcher.js';
import { expect, use } from 'chai';
import sinonChai from 'sinon-chai';
import SignalsPlugin from '../src/index.js';
import BackbonePlugin from '@vaadin/hilla-generator-plugin-backbone';

use(sinonChai);
use(snapshotMatcher);

describe('SignalsPlugin', () => {
  context('Endpoint methods with Signals as return type', () => {
    it('correctly generates service wrapper', async () => {
      const generator = new Generator([BackbonePlugin, SignalsPlugin], {
        logger: new LoggerFactory({ name: 'model-plugin-test', verbose: true }),
      });
      const input = await readFile(new URL('./hilla-openapi.json', import.meta.url), 'utf8');
      const files = await generator.process(input);

      let i = 0;
      for (const file of files) {
        await expect(await file.text()).toMatchSnapshot(`number-signal-${i}`, import.meta.url);
        i++;
      }
    });
  });
});
