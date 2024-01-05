import { readdir, readFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import Generator from '@vaadin/hilla-generator-core/Generator.js';
import LoggerFactory from '@vaadin/hilla-generator-utils/LoggerFactory.js';
import snapshotMatcher from '@vaadin/hilla-generator-utils/testing/snapshotMatcher.js';
import { expect, use } from 'chai';
import sinonChai from 'sinon-chai';
import ModelPlugin from '../../src/index.js';

use(sinonChai);
use(snapshotMatcher);

describe('FormPlugin', () => {
  context('models', () => {
    it('correctly generates code', async () => {
      const modelNames = (await readdir(fileURLToPath(new URL('./fixtures', import.meta.url))))
        .filter((fname) => !fname.startsWith('.')) // exclude .DS_Store and such
        .map((fname) => fname.substring(0, fname.length - 8));

      const generator = new Generator([ModelPlugin], {
        logger: new LoggerFactory({ name: 'model-plugin-test', verbose: true }),
      });
      const input = await readFile(new URL('./Model.json', import.meta.url), 'utf8');
      const files = await generator.process(input);
      expect(files.length, 'number of generated files').to.equal(modelNames.length);

      const filesByName = files.reduce<Record<string, (typeof files)[0]>>((r, file) => {
        r[file.name] = file;
        return r;
      }, {});

      await Promise.all(
        modelNames.map(async (modelName) => {
          const fileName = `com/example/application/endpoints/TsFormEndpoint/${modelName}.ts`;
          const modelFile = filesByName[fileName];
          expect(modelFile, `${fileName} file`).to.not.be.undefined;
          await expect(await modelFile.text(), `${fileName} file`).toMatchSnapshot(modelName, import.meta.url);
        }),
      );
    });
  });
});
