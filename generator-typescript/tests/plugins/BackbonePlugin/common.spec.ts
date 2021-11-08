/* eslint-disable import/no-extraneous-dependencies */
import { expect, use } from 'chai';
import sinonChai from 'sinon-chai';
import BackbonePlugin from '../../../src/plugins/BackbonePlugin/index.js';
import { createGenerator, loadInput } from '../../utils/common.js';
import snapshotMatcher from '../../utils/snapshotMatcher.js';

use(sinonChai);
use(snapshotMatcher);

describe('common', () => {
  it('correctly generates code for CollectionEndpoint', async () => {
    const endpointName = 'CollectionEndpoint';
    const generator = createGenerator([BackbonePlugin]);
    const input = await loadInput(endpointName, import.meta.url);
    const files = await generator.process(input);
    expect(files.length).to.equal(3);

    const [, endpointFile, collectionEntityFile] = files;

    await expect(await endpointFile.text()).toMatchSnapshot(endpointName, import.meta.url);
    await expect(await collectionEntityFile.text()).toMatchSnapshot('Collection.entity', import.meta.url);
    expect(endpointFile.name).to.equal('./CollectionEndpoint.ts');
    expect(collectionEntityFile.name).to.equal(
      './com/vaadin/fusion/parser/plugins/backbone/collectionendpoint/CollectionEndpoint/Collection.ts',
    );
  });
});
