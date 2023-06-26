import { chmod, mkdtemp, rm, writeFile } from 'fs/promises';
import { join } from 'path';
import LoggerFactory from '@hilla/generator-typescript-utils/LoggerFactory.js';
import { expect } from 'chai';
import GeneratorIO from '../src/GeneratorIO.js';

describe('Testing GeneratorIO', () => {
  const logger = new LoggerFactory({ verbose: true });
  const generatedFilenames = [1, 2, 3].map((i) => `file${i}.ts`);
  let tmpDir: string;
  let io: GeneratorIO;

  beforeEach(async () => {
    tmpDir = await mkdtemp('generator-io-test-');
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
      expect(await io.exists(path)).to.be.true;
    });

    it("should detect that a file doesn't exist", async () => {
      const path = join(tmpDir, 'nobody-created-me');
      expect(await io.exists(path)).to.be.false;
    });
  });

  describe('Testing GeneratorIO.createIndex', () => {
    it('should create file index with right content', async () => {
      await io.createFileIndex(generatedFilenames);
      const indexPath = join(tmpDir, GeneratorIO.INDEX_FILENAME);
      expect(await io.exists(indexPath)).to.be.true;
      const content = await io.read(indexPath);
      generatedFilenames.forEach((name) => expect(content).to.contain(name));
    });
  });

  describe('Testing GeneratorIO.cleanOutputDir', () => {
    it("should do nothing when there's no file index", async () => {
      const deletedFiles = await io.cleanOutputDir();
      expect(deletedFiles).to.be.empty;

      await Promise.all(
        generatedFilenames.map(async (name) => {
          const path = join(tmpDir, name);
          expect(await io.exists(path)).to.be.true;
        }),
      );
    });

    it('should delete all generated files and report them', async () => {
      await io.createFileIndex(generatedFilenames);
      const deletedFiles = await io.cleanOutputDir();
      expect(deletedFiles.size).to.be.equal(generatedFilenames.length);

      await Promise.all(
        generatedFilenames.map(async (name) => {
          expect(await io.exists(join(tmpDir, name))).to.be.false;
        }),
      );
    });

    it('should not touch other files', async () => {
      await io.createFileIndex(generatedFilenames);
      const name = 'other-file.ts';
      await writeFile(join(tmpDir, name), 'dummy content');
      const deletedFiles = await io.cleanOutputDir();
      expect(deletedFiles.size).to.be.equal(generatedFilenames.length);
      expect(await io.exists(join(tmpDir, name))).to.be.true;
    });

    it('should fail when IO error happens', async () => {
      await chmod(tmpDir, 0o666);

      try {
        await io.cleanOutputDir();
        expect(0).to.be.equal(1); // fail as this shouldn't be reached
      } catch (e: unknown) {
        expect((e as NodeJS.ErrnoException).code).to.not.equal('ENOENT');
      } finally {
        await chmod(tmpDir, 0o777);
      }
    });
  });
});
