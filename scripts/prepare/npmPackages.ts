import type { Version, Versions } from './config.js';

// Matches an `@NpmPackage` annotation, as the generator of the versions files
// in vaadin/flow-components does.
const ANNOTATION = /@NpmPackage\(\s*value\s*=\s*"([^"]+)"\s*,\s*version\s*=\s*"([^"]+)"\s*\)/gu;

// The packages are declared for the Lit mode, as the platform declared them
// and as the component jars pin them: the web components of a Flow component
// are what a Lit application installs, while a React application gets them
// from `@vaadin/react-components`.
const MODE = 'lit';

/**
 * Reads the npm packages a Java source declares with `@NpmPackage`, by package
 * name. A package declared by several classes of one module, as the classes of
 * one web component do, is expected to declare the same version everywhere.
 */
export function parseNpmPackages(source: string, origin = 'the source'): ReadonlyMap<string, string> {
  const packages = new Map<string, string>();

  for (const [, npmName, version] of source.matchAll(ANNOTATION)) {
    const declared = packages.get(npmName);
    if (declared !== undefined && declared !== version) {
      throw new Error(`Conflicting versions for ${npmName} in ${origin}: '${declared}' and '${version}'`);
    }
    packages.set(npmName, version);
  }

  return packages;
}

/**
 * Downloads the given Java sources, each once, so that everything read from
 * them comes from the same revision of the files.
 */
export async function fetchSources(urls: readonly URL[]): Promise<ReadonlyArray<readonly [URL, string]>> {
  return Promise.all(
    urls.map(async (url) => {
      const res = await fetch(url);
      if (!res.ok) {
        throw new Error(`Could not read ${url.toString()}: ${res.status} ${res.statusText}`);
      }
      return [url, await res.text()] as const;
    }),
  );
}

/**
 * Reads the npm packages the given Java sources declare, by package name. The
 * same package may be declared by several of them, as long as the version
 * matches, which is what the version update in vaadin/flow-components keeps it
 * at.
 */
export function npmPackagesIn(sources: ReadonlyArray<readonly [URL, string]>): ReadonlyMap<string, string> {
  const packages = new Map<string, string>();

  for (const [url, contents] of sources) {
    for (const [npmName, version] of parseNpmPackages(contents, url.toString())) {
      const declared = packages.get(npmName);
      if (declared !== undefined && declared !== version) {
        throw new Error(`Conflicting versions for ${npmName} in ${url.toString()}: '${declared}' and '${version}'`);
      }
      packages.set(npmName, version);
    }
  }

  return packages;
}

/**
 * Finds the version any section of the platform versions declares for an npm
 * package, by the `npmName` the entry carries rather than by its own name,
 * which is only a label.
 */
export function findNpmVersion(versions: Versions, npmName: string): string | undefined {
  for (const section of Object.values(versions)) {
    if (typeof section !== 'object') {
      continue;
    }
    for (const entry of Object.values(section as Record<string, Version>)) {
      if (entry.npmName === npmName && entry.jsVersion) {
        return entry.jsVersion;
      }
    }
  }

  return undefined;
}

/**
 * Adds the npm packages read from the component annotations to the `core`
 * section of the platform versions, leaving alone any package the platform
 * declares itself. That way the versions the platform still declares stay
 * authoritative, on a maintenance branch that declares them all, and the
 * annotations fill in only what it has stopped declaring.
 *
 * An entry is named after the package without its scope, as the platform names
 * them and as the versions files of the component jars do.
 */
export function withNpmPackages(versions: Versions, packages: ReadonlyMap<string, string>): Versions {
  const core = { ...versions.core };

  for (const [npmName, jsVersion] of packages) {
    if (findNpmVersion(versions, npmName) !== undefined) {
      continue;
    }
    core[npmName.replace(/^@[^/]+\//u, '')] = { jsVersion, mode: MODE, npmName };
  }

  return { ...versions, core };
}

const REACT_COMPONENTS = /REACT_COMPONENTS\s*=\s*Map\.of\(([\s\S]*?)\);/u;

const REACT_COMPONENT = /"([^"]+)"\s*,\s*List\.of\(([^)]*)\)/gu;

const STRING = /"([^"]+)"/gu;

/**
 * Reads the React component packages a Java source declares in its
 * `REACT_COMPONENTS` map, each with the web component packages it brings,
 * which a React application leaves out of its `package.json`. They are
 * released with the components, so their version is the one the annotations
 * of the same source declare, which have to agree on it.
 */
export function parseReactComponents(source: string, origin = 'the source'): Versions['react'] {
  const map = REACT_COMPONENTS.exec(source);
  if (!map) {
    return {};
  }

  const versions = new Set(parseNpmPackages(source, origin).values());
  if (versions.size !== 1) {
    throw new Error(
      `The React components in ${origin} take the version of its annotations, which declare ${versions.size === 0 ? 'none' : [...versions].join(', ')}`,
    );
  }
  const [jsVersion] = versions;

  const react: Versions['react'] = {};

  for (const [, npmName, packages] of map[1].matchAll(REACT_COMPONENT)) {
    react[npmName.replace(/^@[^/]+\//u, '')] = {
      exclusions: [...packages.matchAll(STRING)].map(([, excluded]) => excluded),
      jsVersion,
      mode: 'react',
      npmName,
    };
  }

  return react;
}

/**
 * Reads the React component packages the given Java sources declare.
 */
export function reactComponentsIn(sources: ReadonlyArray<readonly [URL, string]>): Versions['react'] {
  return Object.assign(
    {},
    ...sources.map(([url, contents]) => parseReactComponents(contents, url.toString())),
  ) as Versions['react'];
}

/**
 * Adds the React component packages read from the component sources to the
 * `react` section of the platform versions, leaving alone any the platform
 * declares itself, as a maintenance branch still does.
 */
export function withReactComponents(versions: Versions, react: Versions['react']): Versions {
  const merged = { ...versions.react };

  for (const [name, entry] of Object.entries(react)) {
    if (entry.npmName && findNpmVersion(versions, entry.npmName) === undefined) {
      merged[name] = entry;
    }
  }

  return { ...versions, react: merged };
}
