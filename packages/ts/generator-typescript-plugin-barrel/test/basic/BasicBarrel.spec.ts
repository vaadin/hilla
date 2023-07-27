/* eslint-disable import/no-extraneous-dependencies */
import { readFile } from 'node:fs/promises';
import Generator from '@hilla/generator-typescript-core/Generator.js';
import BackbonePlugin from '@hilla/generator-typescript-plugin-backbone';
import LoggerFactory from '@hilla/generator-typescript-utils/LoggerFactory.js';
import snapshotMatcher from '@hilla/generator-typescript-utils/testing/snapshotMatcher.js';
import { expect, use } from 'chai';
import sinonChai from 'sinon-chai';
import BarrelPlugin from '../../src/index.js';

use(sinonChai);
use(snapshotMatcher);

describe('BarrelPlugin', () => {
  const sectionName = 'BasicBarrel';

  it('correctly generates code', async () => {
    const generator = new Generator([BackbonePlugin, BarrelPlugin], {
      logger: new LoggerFactory({ name: 'barrel-plugin-test', verbose: true }),
    });
    const input = await readFile(new URL(`./${sectionName}.json`, import.meta.url), 'utf8');
    const files = await generator.process(input);

    const barrelFile = files[files.length - 1];

    await expect(await barrelFile.text()).toMatchSnapshot(`barrel`, import.meta.url);
    expect(barrelFile.name).to.equal(`endpoints.ts`);
  });
});
