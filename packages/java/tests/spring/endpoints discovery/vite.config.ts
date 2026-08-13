import { defineConfig } from 'vite';
import checker from 'vite-plugin-checker';
import { vaadinConfig } from './vite.generated';

// This project deliberately lives in a directory whose name contains a space.
//
// The generated Vaadin config points the TypeScript checker at an absolute
// project root, which vite-plugin-checker turns into a "tsc -p <root>" command
// line and runs through a shell without quoting it, so the path splits in two
// and the frontend build fails with "TS5042: Option 'project' cannot be mixed
// with source files on a command line".
//
// A relative root names the same tsconfig.json, because Flow always starts Vite
// with the project root as the working directory, and has no space to split on.
// Everything else is left as generated.
//
// Remove this once vite-plugin-checker quotes its arguments
// (https://github.com/fi3ework/vite-plugin-checker/pull/792) or Flow stops
// passing an absolute root.
export default defineConfig(async (env) => {
  const config = await vaadinConfig(env);

  config.plugins = config.plugins?.map((plugin) =>
    plugin && typeof plugin === 'object' && 'name' in plugin && plugin.name === 'vite-plugin-checker'
      ? checker({ typescript: { root: '.' } })
      : plugin,
  );

  return config;
});
