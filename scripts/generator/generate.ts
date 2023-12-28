import { createRequire } from 'node:module';
import { fileURLToPath } from 'node:url';
import { Script } from 'node:vm';
import { remote, type Transformer, type Versions, type Writer } from '../config.js';

const require = createRequire(import.meta.url);

async function loadScripts(): Promise<readonly [Writer, Transformer]> {
  const [creator, replacer, transformer, writer] = await Promise.all(
    ['creator', 'replacer', 'transformer', 'writer'].map(async (name) => {
      const url = new URL(`./${name}.js`, remote.src);
      const res = await fetch(url);
      const code = await res.text();
      return new Script(code, { filename: url.toString() });
    }),
  );

  function req(name: string): unknown {
    let script: Script;

    if (name.includes('creator')) {
      script = creator;
    } else if (name.includes('replacer')) {
      script = replacer;
    } else if (name.includes('writer')) {
      script = writer;
    } else if (name.includes('transformer')) {
      script = transformer;
    } else {
      return require(name);
    }

    const exports = {};

    const ctx = {
      exports,
      module: { exports },
      require: req,
    };
    script.runInNewContext(ctx);

    return ctx.module.exports;
  }

  return [req('writer') as Writer, req('transformer') as Transformer];
}

const [writer, transformer] = await loadScripts();

const [
  hillaJsonTemplateFileName,
  hillaReactJsonTemplateFileName,
  releaseNotesTemplateFileName,
  releaseNotesMaintenanceTemplateFileName,
  releaseNotesPrereleaseTemplateFileName,
] = [
  'template-hilla-versions.json',
  'template-hilla-react-versions.json',
  'template-release-note.md',
  'template-release-note-maintenance.md',
  'template-release-note-prerelease.md',
].map((name) => fileURLToPath(new URL(`templates/${name}`, import.meta.url)));

const [
  hillaJsonResultFileName,
  hillaReactJsonResultFileName,
  releaseNotesResultFileName,
  releaseNotesMaintenanceResultFileName,
  releaseNotesPrereleaseResultFileName,
] = [
  'hilla-versions.json',
  'hilla-react-versions.json',
  'release-note.md',
  'release-note-maintenance.md',
  'release-note-prerelease.md',
].map((name) => fileURLToPath(new URL(`results/${name}`, import.meta.url)));

export default function generate(version: string, versions: Versions): void {
  const transformed = transformer.transformVersions(versions, version, false);
  transformed.platform = version;

  writer.writeSeparateJson(versions.bundles, hillaJsonTemplateFileName, hillaJsonResultFileName, 'bundles');
  writer.writeSeparateJson(versions.core, hillaJsonTemplateFileName, hillaJsonResultFileName, 'core');
  writer.writeSeparateJson(versions.vaadin, hillaJsonTemplateFileName, hillaJsonResultFileName, 'vaadin');
  writer.writeSeparateJson(versions.bundles, hillaReactJsonTemplateFileName, hillaReactJsonResultFileName, 'bundles');
  writer.writeSeparateJson(versions.react, hillaReactJsonTemplateFileName, hillaReactJsonResultFileName, 'react');

  writer.writeReleaseNotes(versions, releaseNotesTemplateFileName, releaseNotesResultFileName);
  writer.writeReleaseNotes(versions, releaseNotesMaintenanceTemplateFileName, releaseNotesMaintenanceResultFileName);
  writer.writeReleaseNotes(versions, releaseNotesPrereleaseTemplateFileName, releaseNotesPrereleaseResultFileName);

  transformed.core.hilla = { javaVersion: version };

  // write hilla version to hilla-react-versions.json as platform
  writer.writeSeparateJson(version, hillaJsonTemplateFileName, hillaJsonResultFileName, 'platform');
  writer.writeSeparateJson(version, hillaReactJsonTemplateFileName, hillaReactJsonResultFileName, 'platform');
}
