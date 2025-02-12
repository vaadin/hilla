import { exec } from 'node:child_process';
import { promisify } from 'node:util';
import chaiAsPromised from 'chai-as-promised';
import { describe, expect, it, chai } from 'vitest';

chai.use(chaiAsPromised);

const execAsync = promisify(exec);

describe('cli', () => {
  it('should print help', async () => {
    const { stdout } = await execAsync(`npx tsx src/index.ts --help`);
    await expect(stdout).toMatchFileSnapshot('fixtures/help.snap');
  });

  it('should print version', async () => {
    const { stdout } = await execAsync(`npx tsx src/index.ts --version`);
    await expect(stdout).toMatchFileSnapshot('fixtures/version.snap');
  });

  it('should throw an error if input file is not provided', async () => {
    await expect(execAsync(`npx tsx src/index.ts`)).to.eventually.be.rejectedWith('Input file is required');
  });
});
