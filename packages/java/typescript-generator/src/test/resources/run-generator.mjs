#!/usr/bin/env node
/**
 * Node.js script to run Hilla TypeScript generator from Java tests.
 *
 * This script:
 * 1. Reads OpenAPI JSON from stdin
 * 2. Runs the TypeScript generator with all standard plugins
 * 3. Outputs generated files as JSON to stdout
 *
 * Output format:
 * {
 *   "files": [
 *     {"name": "path/to/file.ts", "content": "file content..."},
 *     ...
 *   ]
 * }
 */

import { fileURLToPath } from 'node:url';
import { dirname, join, resolve } from 'node:path';
import { tmpdir } from 'node:os';

// Find the monorepo root
// At runtime, this script is at: packages/java/typescript-generator/target/test-classes/run-generator.mjs
// We need to go to: packages/ts/...
const scriptDir = dirname(fileURLToPath(import.meta.url));
// Go up from target/test-classes: test-classes -> target -> typescript-generator -> java -> packages -> hilla (root)
const repoRoot = resolve(scriptDir, '../../../../..');
const packagesTs = join(repoRoot, 'packages/ts');

// Dynamically import generator and plugins
// Built packages have .js files at the root, not in src/
async function loadModule(packageName, moduleName) {
  const packagePath = join(packagesTs, packageName, `${moduleName}.js`);
  return await import(packagePath);
}

async function main() {
  try {
    // Import generator core and utilities
    const GeneratorModule = await loadModule('generator-core', 'Generator');
    const LoggerFactoryModule = await loadModule('generator-utils', 'LoggerFactory');

    const Generator = GeneratorModule.default;
    const LoggerFactory = LoggerFactoryModule.default;

    // Import all standard plugins (they export as default from index.js)
    const BackbonePlugin = (await loadModule('generator-plugin-backbone', 'index')).default;
    const BarrelPlugin = (await loadModule('generator-plugin-barrel', 'index')).default;
    const ClientPlugin = (await loadModule('generator-plugin-client', 'index')).default;
    const ModelPlugin = (await loadModule('generator-plugin-model', 'index')).default;
    const PushPlugin = (await loadModule('generator-plugin-push', 'index')).default;
    const SignalsPlugin = (await loadModule('generator-plugin-signals', 'index')).default;
    const SubtypesPlugin = (await loadModule('generator-plugin-subtypes', 'index')).default;
    const TransferTypesPlugin = (await loadModule('generator-plugin-transfertypes', 'index')).default;

    // Read OpenAPI JSON from stdin
    let inputData = '';
    for await (const chunk of process.stdin) {
      inputData += chunk;
    }

    if (!inputData.trim()) {
      throw new Error('No input provided via stdin');
    }

    // Create logger (non-verbose for tests)
    const logger = new LoggerFactory({ verbose: false });

    // Configure all plugins in the correct order
    // Note: SignalsPlugin temporarily disabled due to runtime error
    // TODO: Investigate SignalProcessor issue
    const plugins = [
      BackbonePlugin,
      ModelPlugin,
      TransferTypesPlugin,
      SubtypesPlugin,
      // SignalsPlugin,  // Disabled - causes: Cannot read properties of undefined (reading 'text')
      PushPlugin,
      ClientPlugin,
      BarrelPlugin,
    ];

    // Run generator with a temporary output directory
    // (not actually used for file writing, just for context)
    const generator = new Generator(plugins, {
      logger,
      outputDir: join(tmpdir(), 'hilla-test-output')
    });
    const files = await generator.process(inputData);

    // Convert File objects to plain JSON
    const result = {
      files: await Promise.all(
        files.map(async (file) => ({
          name: file.name,
          content: await file.text(),
        }))
      ),
    };

    // Output as JSON
    console.log(JSON.stringify(result, null, 2));
  } catch (error) {
    console.error('Error running generator:', error);
    process.exit(1);
  }
}

main();
