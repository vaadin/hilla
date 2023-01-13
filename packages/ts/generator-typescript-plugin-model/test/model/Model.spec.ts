import Generator from '@hilla/generator-typescript-core/Generator.js';
import LoggerFactory from '@hilla/generator-typescript-utils/LoggerFactory.js';
import snapshotMatcher from '@hilla/generator-typescript-utils/testing/snapshotMatcher.js';
import { expect, use } from 'chai';
import { readdir, readFile } from 'fs/promises';
import sinonChai from 'sinon-chai';
import { fileURLToPath, URL } from 'url';
import ModelPlugin from '../../src/index.js';

use(sinonChai);
use(snapshotMatcher);

describe('FormPlugin', () => {
  context('models', () => {
    it('correctly generates code', async () => {
      const modelNames = (await readdir(fileURLToPath(new URL('./fixtures', import.meta.url)))).map((fname) =>
        fname.substring(0, fname.length - 8),
      );

      const generator = new Generator([ModelPlugin], {
        logger: new LoggerFactory({ name: 'model-plugin-test', verbose: true }),
      });
      const input = await readFile(new URL('./Model.json', import.meta.url), 'utf8');
      const files = await generator.process(input);
      expect(files.length).to.equal(modelNames.length);

      const filesByName = files.reduce<Record<string, (typeof files)[0]>>((r, file) => {
        r[file.name] = file;
        return r;
      }, {});

      await Promise.all(
        modelNames.map(async (modelName) => {
          const fileName = `com/example/application/endpoints/TsFormEndpoint/${modelName}.ts`;
          const modelFile = filesByName[fileName];
          expect(modelFile).to.not.be.undefined;
          await expect(await modelFile.text()).toMatchSnapshot(modelName, import.meta.url);
        }),
      );
    });
  });
});
