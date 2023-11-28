import { rename } from 'fs/promises';
import { glob } from 'glob';

const root = new URL('../', import.meta.url);

const packed = await glob('*.tgz', { cwd: root });
const pattern = /hilla-(.*)-\d+\.\d+\.\d+/u;

await Promise.all(
  packed
    .filter((file) => pattern.test(file))
    .map(async (file) => {
      const fileUrl = new URL(file, root);
      const [, name] = pattern.exec(file) ?? [];
      const newUrl = new URL(`hilla-${name}.tgz`, root);
      await rename(fileUrl, newUrl);
      // eslint-disable-next-line no-console
      console.log(newUrl.toString());
    }),
);
