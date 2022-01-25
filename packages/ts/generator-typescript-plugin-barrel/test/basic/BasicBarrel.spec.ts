/* eslint-disable import/no-extraneous-dependencies */
import Generator from '@vaadin/generator-typescript-core/Generator.js';
import BackbonePlugin from '@vaadin/generator-typescript-plugin-backbone';
import LoggerFactory from '@vaadin/generator-typescript-utils/LoggerFactory.js';
import snapshotMatcher from '@vaadin/generator-typescript-utils/testing/snapshotMatcher.js';
import { expect, use } from 'chai';
import { readFile } from 'fs/promises';
import sinonChai from 'sinon-chai';
import { URL } from 'url';
import BarrelPlugin from '../../src/index.js';

use(sinonChai);
use(snapshotMatcher);

describe('BarrelPlugin', () => {
  const sectionName = 'BasicBarrel';

  it('correctly generates code', async () => {
    const generator = new Generator(
      [BackbonePlugin, BarrelPlugin],
      new LoggerFactory({ name: 'barrel-plugin-test', verbose: true }),
    );
    const input = await readFile(new URL(`./${sectionName}.json`, import.meta.url), 'utf8');
    const files = await generator.process(input);

    const barrelFile = files[files.length - 1];

    await expect(await barrelFile.text()).toMatchSnapshot(`barrel`, import.meta.url);
    expect(barrelFile.name).to.equal(`endpoints.ts`);
  });
});
