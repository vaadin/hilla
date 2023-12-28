export type Version = {
  javaVersion?: string;
  jsVersion?: string;
  npmName?: string;
};

export type Versions = {
  bundles: Record<string, Version>;
  core: Record<string, Version>;
  kits: Record<string, Version>;
  platform: string;
  react: Record<string, Version>;
  vaadin: Record<string, Version>;
};

export interface Writer {
  writeReleaseNotes(versions: Versions, templateFileName: string, resultFileName: string): void;
  writeSeparateJson(
    versions: Record<string, Version> | string,
    templateFileName: string,
    resultFileName: string,
    key: keyof Versions,
  ): void;
}

export interface Transformer {
  transformVersions(versions: Versions, version: string, isPrerelease: boolean): Versions;
}

// TODO: compute this number when we maintain multiple hilla branches
export const branch = '24.3.0';

export const repoUrl = new URL('https://raw.githubusercontent.com/vaadin/');
export const root = new URL('../', import.meta.url);
export const rel = {
  src: 'scripts/generator/src',
  results: 'scripts/generator/results',
};

export const local = {
  src: new URL(`${rel.src}/`, root),
  versionedPackageJson: new URL('packages/ts/generator-typescript-core/package.json', root),
  results: new URL(`${rel.results}/`, root),
};

export const remote = {
  // https://raw.githubusercontent.com/vaadin/platform/24.3.0/scripts/generator/src/writer.js
  src: new URL(`platform/${branch}/${rel.src}/`, repoUrl),
  versions: new URL(`platform/${branch}/versions.json`, repoUrl),
  lumo: new URL(
    `flow-components/${branch}/vaadin-lumo-theme-flow-parent/vaadin-lumo-theme-flow/src/main/java/com/vaadin/flow/theme/lumo/Lumo.java`,
    repoUrl,
  ),
  material: new URL(
    `flow-components/${branch}/vaadin-material-theme-flow-parent/vaadin-material-theme-flow/src/main/java/com/vaadin/flow/theme/material/Material.java`,
    repoUrl,
  ),
};

export const destination = {
  lit: {
    versions: new URL('packages/java/hilla/hilla-versions.json', root),
    themeDir: new URL('packages/java/hilla/src/main/java/dev/hilla/theme/', root),
  },
  react: {
    versions: new URL('packages/java/hilla-react/hilla-react-versions.json', root),
    themeDir: new URL('packages/java/hilla-react/src/main/java/dev/hilla/theme/', root),
  },
};
