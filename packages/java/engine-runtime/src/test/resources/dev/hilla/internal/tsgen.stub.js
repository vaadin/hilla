#!/usr/bin/env node
const fs = require('node:fs/promises');
const path = require('node:path');

async function main() {
  console.log('Running test stub TypeScript generator...');
  const outputDir = process.argv[process.argv.indexOf('-o') + 1];
  await fs.mkdir(outputDir, { recursive: true });
  await fs.writeFile(path.resolve(outputDir, 'connect-client.default.ts'), '', { encoding: 'utf-8' });
  await fs.writeFile(path.resolve(outputDir, 'MyEndpoint.ts'), '', { encoding: 'utf-8' });
}

main().then(() => process.exit(0));
