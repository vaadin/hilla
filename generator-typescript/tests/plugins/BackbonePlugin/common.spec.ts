/* eslint-disable import/no-extraneous-dependencies */
import { expect, use } from 'chai';
import { readFile } from 'fs/promises';
import sinonChai from 'sinon-chai';
import { fileURLToPath, URL } from 'url';
import BackbonePlugin from '../../../src/plugins/BackbonePlugin/index.js';
import { createGenerator } from '../../utils/common.js';
import snapshotMatcher from '../../utils/snapshotMatcher.js';

use(sinonChai);
use(snapshotMatcher);

function createTest(testName: string): () => Promise<void> {
  return async () => {
    const generator = createGenerator([BackbonePlugin]);
    const jsonUrl = new URL(`./resources/${testName}.json`, import.meta.url);
    const input = await readFile(fileURLToPath(jsonUrl), 'utf8');
    const files = await generator.process(input);
    expect(files.length).to.equal(1);
    await expect(await files[0].text()).toMatchSnapshot(testName, import.meta.url);
  };
}

describe('common', () => {
  it('correctly generates code for CollectionEndpoint', createTest('CollectionEndpoint'));
});
