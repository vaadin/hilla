/* eslint-disable import/no-extraneous-dependencies */
import { readFile } from 'node:fs/promises';
import Generator from '@vaadin/hilla-generator-core/Generator.js';
import BackbonePlugin from '@vaadin/hilla-generator-plugin-backbone';
import LoggerFactory from '@vaadin/hilla-generator-utils/LoggerFactory.js';
import sinonChai from 'sinon-chai';
import { chai, describe, expect, it } from 'vitest';
import BarrelPlugin from '../../src/index.js';

chai.use(sinonChai);

describe('BarrelPlugin', () => {
  const sectionName = 'BasicBarrel';

  it('correctly generates code', async () => {
    const generator = new Generator([BackbonePlugin, BarrelPlugin], {
      logger: new LoggerFactory({ name: 'barrel-plugin-test', verbose: true }),
    });
    const input = await readFile(new URL(`./${sectionName}.json`, import.meta.url), 'utf8');
    const files = await generator.process(input);

    const barrelFile = files[files.length - 1];

    await expect(await barrelFile.text()).toMatchFileSnapshot(`fixtures/barrel.snap.ts`);
    expect(barrelFile.name).to.equal(`endpoints.ts`);
  });
});
