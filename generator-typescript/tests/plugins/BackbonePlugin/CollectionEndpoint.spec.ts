/* eslint-disable import/no-extraneous-dependencies */
import { expect, use } from 'chai';
import { readFile } from 'fs/promises';
import { dirname, resolve } from 'path';
import sinonChai from 'sinon-chai';
import { fileURLToPath } from 'url';
import type Generator from '../../../src/core/Generator.js';
import BackbonePlugin from '../../../src/plugins/BackbonePlugin/index.js';
import { createGenerator } from '../../utils.js';

use(sinonChai);

describe('Collection Endpoint', () => {
  let generator: Generator;

  beforeEach(async () => {
    generator = createGenerator([BackbonePlugin]);
  });

  it('correctly generates code from OpenAPI document', async () => {
    const filePath = fileURLToPath(import.meta.url);
    const [input, sample] = await Promise.all([
      readFile(resolve(dirname(filePath), './CollectionEndpoint.json'), 'utf8'),
      readFile(resolve(dirname(filePath), './CollectionEndpoint.sample.ts'), 'utf8'),
    ]);
    const files = await generator.process(input);
    expect(files.length).to.equal(1);
    expect(await files[0].text()).to.equal(sample);
  });
});
