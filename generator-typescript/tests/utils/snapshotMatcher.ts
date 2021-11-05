import { readFile, writeFile } from 'fs/promises';
import { fileURLToPath, URL } from 'url';

const argv = process.argv.slice(2);
const shouldUpdate = argv.includes('-u') || argv.includes('--update');

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  export namespace Chai {
    interface Assertion {
      toMatchSnapshot(snapshotName: string, importMetaUrl: string): Promise<void>;
    }
  }
}

export default function snapshotMatcher(chai: Chai.ChaiStatic, utils: Chai.ChaiUtils) {
  utils.addMethod(
    chai.Assertion.prototype,
    'toMatchSnapshot',
    // eslint-disable-next-line @typescript-eslint/ban-types
    async function (this: object, snapshotName: string, importMetaUrl: string) {
      const obj = utils.flag(this, 'object');
      const snapshotURL = new URL(`./resources/${snapshotName}.snap.ts`, importMetaUrl);
      const snapshotPath = fileURLToPath(snapshotURL);

      if (shouldUpdate) {
        await writeFile(snapshotPath, obj, 'utf8');
      } else {
        let snapshot;
        try {
          snapshot = await readFile(snapshotPath, 'utf8');
        } catch (e) {
          throw new Error(
            `Snapshot does not exist yet: ${snapshotURL}.\nConsider running tests with -u or --update flag.`,
          );
        }

        chai.assert.equal(snapshot, obj);
      }
    },
  );
}
