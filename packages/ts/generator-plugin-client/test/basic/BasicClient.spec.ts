import { readFile } from 'node:fs/promises';
import Generator from '@vaadin/hilla-generator-core/Generator.js';
import LoggerFactory from '@vaadin/hilla-generator-utils/LoggerFactory.js';
import snapshotMatcher from '@vaadin/hilla-generator-utils/testing/snapshotMatcher.js';
import { expect, use } from 'chai';
import sinonChai from 'sinon-chai';
import ClientPlugin from '../../src/index.js';

use(sinonChai);
use(snapshotMatcher);

describe('ClientPlugin', () => {
  const sectionName = 'BasicClient';

  it('correctly generates code', async () => {
    const generator = new Generator([ClientPlugin], {
      logger: new LoggerFactory({ name: 'client-plugin-test', verbose: true }),
    });
    const input = await readFile(new URL(`./${sectionName}.json`, import.meta.url), 'utf8');
    const files = await generator.process(input);

    const [clientFile] = files;

    await expect(await clientFile.text()).toMatchSnapshot(`client`, import.meta.url);
    expect(clientFile.name).to.equal(`connect-client.default.ts`);
  });
});
