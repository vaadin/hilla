import type * as FsPromises from 'node:fs/promises';
import { mkdir, mkdtemp, readFile, rm, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { pathToFileURL } from 'node:url';
import Generator from '@vaadin/hilla-generator-core/Generator.js';
import LoggerFactory from '@vaadin/hilla-generator-utils/LoggerFactory.js';
import sinonChai from 'sinon-chai';
import { afterAll, afterEach, beforeAll, chai, describe, expect, it, vi } from 'vitest';
import ClientPlugin from '../../src/index.js';

chai.use(sinonChai);

const closeSpies = vi.hoisted(() => [] as unknown[]);

vi.mock('fs/promises', async (importOriginal) => {
  const original = await importOriginal<typeof FsPromises>();
  return {
    ...original,
    async open(...args: Parameters<typeof original.open>) {
      const handle = await original.open(...args);
      closeSpies.push(vi.spyOn(handle, 'close'));
      return handle;
    },
  };
});

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

  describe('custom client detection', () => {
    let root: string;
    let outputDir: string;

    beforeAll(async () => {
      root = await mkdtemp(join(tmpdir(), 'client-plugin-'));
      outputDir = join(root, 'generated');
      await mkdir(outputDir);
    });

    afterAll(async () => {
      await rm(root, { force: true, recursive: true });
    });

    afterEach(async () => {
      closeSpies.length = 0;
      await rm(join(root, 'connect-client.ts'), { force: true });
    });

    it('uses the default client when no directory is given', async () => {
      expect(await ClientPlugin.getClientFileName()).to.equal(ClientPlugin.CLIENT_FILE_NAME);
    });

    it('uses the default client when there is no custom client file', async () => {
      expect(await ClientPlugin.getClientFileName(outputDir)).to.equal(ClientPlugin.CLIENT_FILE_NAME);
    });

    it('uses the custom client file and closes it after the check', async () => {
      await writeFile(join(root, 'connect-client.ts'), '');

      expect(await ClientPlugin.getClientFileName(outputDir)).to.equal(ClientPlugin.CUSTOM_CLIENT_FILE_NAME);
      expect(await ClientPlugin.getClientFileName(pathToFileURL(outputDir).toString())).to.equal(
        ClientPlugin.CUSTOM_CLIENT_FILE_NAME,
      );
      expect(closeSpies).to.have.lengthOf(2);
      closeSpies.forEach((close) => expect(close).toHaveBeenCalledOnce());
    });
  });
});
