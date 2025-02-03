import { readFile } from 'node:fs/promises';
import Generator from '@vaadin/hilla-generator-core/Generator.js';
import LoggerFactory from '@vaadin/hilla-generator-utils/LoggerFactory.js';
import sinonChai from 'sinon-chai';
import { chai, describe, expect, it } from 'vitest';
import ClientPlugin from '../../src/index.js';

chai.use(sinonChai);

describe('ClientPlugin', () => {
  const sectionName = 'BasicClient';

  it('correctly generates code', async () => {
    const generator = new Generator([ClientPlugin], {
      logger: new LoggerFactory({ name: 'client-plugin-test', verbose: true }),
    });
    const input = await readFile(new URL(`./${sectionName}.json`, import.meta.url), 'utf8');
    const files = await generator.process(input);

    const [clientFile] = files;

    await expect(await clientFile.text()).toMatchFileSnapshot(`fixtures/client.snap.ts`);
    expect(clientFile.name).to.equal(`connect-client.default.ts`);
  });
});
