import Generator from '@vaadin/generator-typescript-core/Generator.js';
import createLogger from '@vaadin/generator-typescript-utils/createLogger.js';
import snapshotMatcher from '@vaadin/generator-typescript-utils/testing/snapshotMatcher.js';
import { expect, use } from 'chai';
import { readFile } from 'fs/promises';
import sinonChai from 'sinon-chai';
import { URL } from 'url';
import ClientPlugin from '../../src/index.js';

use(sinonChai);
use(snapshotMatcher);

describe('ClientPlugin', () => {
  const sectionName = 'BasicClient';

  it('correctly generates code', async () => {
    const generator = new Generator([ClientPlugin], createLogger({ name: 'client-plugin-test', verbose: true }));
    const input = await readFile(new URL(`./${sectionName}.json`, import.meta.url), 'utf8');
    const files = await generator.process(input);

    const [clientFile] = files;

    await expect(await clientFile.text()).toMatchSnapshot(`client`, import.meta.url);
    expect(clientFile.name).to.equal(`connect-client.default.ts`);
  });
});
