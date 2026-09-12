import { argv, env } from "node:process";

export type Version = {
  javaVersion?: string;
  jsVersion?: string;
  mode?: string;
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
// The component npm versions come from vaadin/flow-components. Both
// repositories have a branch per minor version under the same name, `main` or
// `25.3`, so that name reads the two sides of one release line, which is why
// the platform branch is followed by default. Only the line is shared: the npm
// version of the components on a branch differs from the platform version in
// the last number, which is why it is read from the annotations rather than
// derived from anything the platform declares.
const envComponentsBranch = env.FLOW_COMPONENTS_BRANCH?.trim();
export const flowComponentsBranch = getArgValue('flow-components-branch') ?? (envComponentsBranch || platformBranch);

export const repoUrl = new URL('https://raw.githubusercontent.com/vaadin/');
export const root = new URL('../../', import.meta.url);

export const componentOptions = ['lit', 'react'];

export const local = {
  src: new URL(`scripts/prepare/src/`, root),
  versionedPackageJson: new URL('packages/ts/generator-core/package.json', root),
  results: new URL(`scripts/prepare/results/`, root),
  components: new URL(`scripts/prepare/templates/components/`, root),
};

// The files in vaadin/flow-components whose `@NpmPackage` annotations declare
// the npm packages Hilla needs a version for. The platform `versions.json` no
// longer declares the component packages: the module that ships a package
// declares the version in the annotation, and pins it in its own jar, which
// makes the annotation the only place the version is written. A package that
// moves to another module has to be listed here under its new path.
const componentSources = [
  // `@vaadin/a11y-base`, `@vaadin/component-base`, `@vaadin/field-base`,
  // `@vaadin/input-container`, `@vaadin/lit-renderer`, `@vaadin/overlay`
  'vaadin-flow-components-shared-parent/vaadin-flow-components-base/src/main/java/com/vaadin/flow/component/shared/internal/TransitiveNpmPackages.java',
  // `@vaadin/vaadin-lumo-styles`, `@vaadin/vaadin-themable-mixin`
  'vaadin-lumo-theme-flow-parent/vaadin-lumo-theme-flow/src/main/java/com/vaadin/flow/theme/lumo/Lumo.java',
];

export const remote = {
  // https://raw.githubusercontent.com/vaadin/platform/24.3.0/scripts/generator/src/writer.js
  src: new URL(`platform/${platformBranch}/scripts/generator/src/`, repoUrl),
  versions: new URL(`platform/${platformBranch}/versions.json`, repoUrl),
  // https://raw.githubusercontent.com/vaadin/flow-components/main/vaadin-lumo-theme-flow-parent/vaadin-lumo-theme-flow/src/main/java/com/vaadin/flow/theme/lumo/Lumo.java
  componentSources: componentSources.map((file) => new URL(`flow-components/${flowComponentsBranch}/${file}`, repoUrl)),
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
