import { exec } from 'node:child_process';
import { promisify } from 'node:util';
import { describe, expect, it } from 'vitest';

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
});
