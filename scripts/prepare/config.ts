import { argv, env } from "node:process";

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

const branch = 'main';

// Parse CLI arguments for branch parameter
const args = argv.slice(2);
function getArgValue(argName: string): string | undefined {
  const argPrefix = `--${argName}=`;
  const arg = args.find((arg) => arg.startsWith(argPrefix))
  return arg?.substring(argPrefix.length)
}
// Also honour PLATFORM_BRANCH from the environment, so CI can point the
// script at a branch without having to expand the npm build script itself.
const envBranch = env.PLATFORM_BRANCH?.trim();
export const platformBranch = getArgValue('platform-branch') ?? (envBranch || branch);

export const repoUrl = new URL('https://raw.githubusercontent.com/vaadin/');
export const root = new URL('../../', import.meta.url);

export const componentOptions = ['lit', 'react'];

export const local = {
  src: new URL(`scripts/prepare/src/`, root),
  versionedPackageJson: new URL('packages/ts/generator-core/package.json', root),
  results: new URL(`scripts/prepare/results/`, root),
  components: new URL(`scripts/prepare/templates/components/`, root),
};

export const remote = {
  // https://raw.githubusercontent.com/vaadin/platform/24.3.0/scripts/generator/src/writer.js
  src: new URL(`platform/${platformBranch}/scripts/generator/src/`, repoUrl),
  versions: new URL(`platform/${platformBranch}/versions.json`, repoUrl),
};

export const destination = {
  versions: [
    new URL('packages/java/hilla/hilla-versions.json', root),
    new URL('packages/java/tests/spring/security/src/main/resources/vaadin-core-versions.json', root),
    new URL('packages/java/tests/spring/security-contextpath/src/main/resources/vaadin-core-versions.json', root),
    new URL('packages/java/tests/spring/security-jwt/src/main/resources/vaadin-core-versions.json', root),
    new URL('packages/java/tests/spring/security-urlmapping/src/main/resources/vaadin-core-versions.json', root),
    new URL('packages/java/tests/spring/react-grid-test/src/main/resources/vaadin-core-versions.json', root),
    new URL('packages/java/tests/spring/react-i18n/src/main/resources/vaadin-core-versions.json', root),
    new URL('packages/java/tests/spring/react-signals/src/main/resources/vaadin-core-versions.json', root),
    new URL('packages/java/tests/gradle/kotlin-gradle-test/src/main/resources/vaadin-core-versions.json', root),
  ],
  components: new URL(
    'packages/java/hilla/src/main/resources/com/vaadin/flow/server/frontend/dependencies/hilla/components/',
    root,
  ),
  reactComponentsInstall: [
    {
      workspace: '@vaadin/hilla-react-crud',
      installFlags: ['--save-prod', '--save-exact'],
    },
  ],
};
