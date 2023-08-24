import { chmod, mkdtemp, rm, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import LoggerFactory from '@hilla/generator-typescript-utils/LoggerFactory.js';
import { expect, use } from 'chai';
import chaiAsPromised from 'chai-as-promised';
import GeneratorIO from '../src/GeneratorIO.js';

use(chaiAsPromised);

describe('Testing GeneratorIO', () => {
  const logger = new LoggerFactory({ verbose: true });
  const generatedFilenames = [1, 2, 3].map((i) => `file${i}.ts`);
  let tmpDir: string;
  let io: GeneratorIO;

  beforeEach(async () => {
    tmpDir = await mkdtemp(join(tmpdir(), 'generator-io-test-'));
    io = new GeneratorIO(tmpDir, logger);
    await Promise.all(
      generatedFilenames.map(async (name) => {
        await writeFile(join(tmpDir, name), 'dummy content');
      }),
    );
  });

  afterEach(async () => {
    await rm(tmpDir, { force: true, recursive: true });
  });

  describe('Testing GeneratorIO.exists', () => {
    it('should detect that a file exists', async () => {
      const path = join(tmpDir, generatedFilenames[0]);
      await expect(io.exists(path)).to.eventually.be.true;
    });

    it("should detect that a file doesn't exist", async () => {
      const path = join(tmpDir, 'nobody-created-me');
      await expect(io.exists(path)).to.eventually.be.false;
    });
  });

  describe('Testing GeneratorIO.createIndex', () => {
    it('should create file index with right content', async () => {
      await io.createFileIndex(generatedFilenames);
      const indexPath = join(tmpDir, GeneratorIO.INDEX_FILENAME);
      await expect(io.exists(indexPath)).to.eventually.be.true;
      const content = await io.read(indexPath);
      generatedFilenames.forEach((name) => expect(content).to.contain(name));
    });
  });

  describe('Testing GeneratorIO.cleanOutputDir', () => {
    it("should do nothing when there's no file index", async () => {
      await expect(io.cleanOutputDir()).to.eventually.be.empty;

      await Promise.all(
        generatedFilenames.map(async (name) => {
          const path = join(tmpDir, name);
          await expect(io.exists(path)).to.eventually.be.true;
        }),
      );
    });

    it('should delete all generated files and report them', async () => {
      await io.createFileIndex(generatedFilenames);
      await expect(io.cleanOutputDir()).to.eventually.have.property('size', generatedFilenames.length);
      const existenceResults = await Promise.all(generatedFilenames.map(async (name) => io.exists(join(tmpDir, name))));
      expect(existenceResults).to.be.deep.equal([false, false, false]);
    });

    it('should not touch other files', async () => {
      await io.createFileIndex(generatedFilenames);
      const name = 'other-file.ts';
      await writeFile(join(tmpDir, name), 'dummy content');
      await expect(io.cleanOutputDir()).to.eventually.have.property('size', generatedFilenames.length);
      await expect(io.exists(join(tmpDir, name))).to.eventually.be.true;
    });

    it('should fail when IO error happens', async () => {
      await chmod(tmpDir, 0o666);
      await expect(io.cleanOutputDir()).to.be.rejectedWith(Error, /^(?!ENOENT).*/u);
      await chmod(tmpDir, 0o777);
    });
  });
});
