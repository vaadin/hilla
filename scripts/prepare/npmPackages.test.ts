import { strict as assert } from 'node:assert';
import { describe, it } from 'node:test';
import type { Versions } from './config.js';
import {
  findNpmVersion,
  parseNpmPackages,
  parseReactComponents,
  withNpmPackages,
  withReactComponents,
} from './npmPackages.js';

const LUMO = `
@NpmPackage(value = "@vaadin/vaadin-themable-mixin", version = "25.3.0-beta2")
@NpmPackage(value = "@vaadin/vaadin-lumo-styles", version = "25.3.0-beta2")
@CssImport("@vaadin/vaadin-lumo-styles/lumo.css")
public class Lumo {}
`;

function versionsWith(core: Versions['core'], react: Versions['react'] = {}): Versions {
  return { bundles: {}, core, kits: {}, platform: '25.3.0-beta2', react, vaadin: {} };
}

describe('parseNpmPackages', () => {
  it('should read every annotated package and ignore the rest of the source', () => {
    assert.deepEqual(
      [...parseNpmPackages(LUMO)],
      [
        ['@vaadin/vaadin-themable-mixin', '25.3.0-beta2'],
        ['@vaadin/vaadin-lumo-styles', '25.3.0-beta2'],
      ],
    );
  });

  it('should accept a package declared twice with the same version', () => {
    const source = `${LUMO}\n@NpmPackage(value = "@vaadin/vaadin-lumo-styles", version = "25.3.0-beta2")`;
    assert.equal(parseNpmPackages(source).get('@vaadin/vaadin-lumo-styles'), '25.3.0-beta2');
  });

  it('should fail on a package declared twice with different versions', () => {
    const source = `${LUMO}\n@NpmPackage(value = "@vaadin/vaadin-lumo-styles", version = "25.3.0-beta1")`;
    assert.throws(() => parseNpmPackages(source, 'Lumo.java'), /Conflicting versions.*lumo-styles.*Lumo\.java/u);
  });
});

describe('findNpmVersion', () => {
  it('should find a package by its npm name in any section', () => {
    const versions = versionsWith(
      { 'vaadin-router': { jsVersion: '2.0.1', npmName: '@vaadin/router' } },
      { 'react-components': { jsVersion: '25.3.0-beta2', npmName: '@vaadin/react-components' } },
    );

    assert.equal(findNpmVersion(versions, '@vaadin/router'), '2.0.1');
    assert.equal(findNpmVersion(versions, '@vaadin/react-components'), '25.3.0-beta2');
    assert.equal(findNpmVersion(versions, '@vaadin/vaadin-lumo-styles'), undefined);
  });

  it('should ignore an entry that declares no npm package', () => {
    assert.equal(findNpmVersion(versionsWith({ flow: { javaVersion: '25.4-SNAPSHOT' } }), '@vaadin/flow'), undefined);
  });
});

describe('withNpmPackages', () => {
  it('should declare a package the platform does not, named without its scope', () => {
    const versions = withNpmPackages(versionsWith({ flow: { javaVersion: '25.4-SNAPSHOT' } }), parseNpmPackages(LUMO));

    assert.deepEqual(versions.core['vaadin-lumo-styles'], {
      jsVersion: '25.3.0-beta2',
      mode: 'lit',
      npmName: '@vaadin/vaadin-lumo-styles',
    });
    assert.equal(findNpmVersion(versions, '@vaadin/vaadin-themable-mixin'), '25.3.0-beta2');
    assert.deepEqual(versions.core.flow, { javaVersion: '25.4-SNAPSHOT' });
  });

  it('should leave a package the platform declares at the platform version', () => {
    const declared = { jsVersion: '25.2.1', mode: 'lit', npmName: '@vaadin/vaadin-lumo-styles' };
    const versions = withNpmPackages(versionsWith({ 'vaadin-lumo-styles': declared }), parseNpmPackages(LUMO));

    assert.deepEqual(versions.core['vaadin-lumo-styles'], declared);
    assert.equal(Object.keys(versions.core).length, 2, 'the package missing from the platform is still added');
  });
});

const TRANSITIVE = `
@NpmPackage(value = "@vaadin/a11y-base", version = "25.4.0-alpha1")
@NpmPackage(value = "@vaadin/component-base", version = "25.4.0-alpha1")
final class TransitiveNpmPackages implements Serializable {
    static final Map<String, List<String>> REACT_COMPONENTS = Map.of(
            "@vaadin/react-components",
            List.of("@vaadin/accordion", "@vaadin/button"),
            "@vaadin/react-components-pro",
            List.of("@vaadin/board", "@vaadin/charts"));
}
`;

describe('parseReactComponents', () => {
  it('should read the React components at the version of the annotations of the source', () => {
    assert.deepEqual(parseReactComponents(TRANSITIVE), {
      'react-components': {
        exclusions: ['@vaadin/accordion', '@vaadin/button'],
        jsVersion: '25.4.0-alpha1',
        mode: 'react',
        npmName: '@vaadin/react-components',
      },
      'react-components-pro': {
        exclusions: ['@vaadin/board', '@vaadin/charts'],
        jsVersion: '25.4.0-alpha1',
        mode: 'react',
        npmName: '@vaadin/react-components-pro',
      },
    });
  });

  it('should read nothing from a source without the map', () => {
    assert.deepEqual(parseReactComponents(LUMO), {});
  });

  it('should fail where the annotations do not say one version', () => {
    const source = TRANSITIVE.replace(
      '"@vaadin/a11y-base", version = "25.4.0-alpha1"',
      '"@vaadin/a11y-base", version = "25.3.0"',
    );
    assert.throws(() => parseReactComponents(source, 'TransitiveNpmPackages.java'), /TransitiveNpmPackages\.java/u);
  });
});

describe('withReactComponents', () => {
  it('should add the React components the platform no longer declares', () => {
    const versions = withReactComponents(versionsWith({}), parseReactComponents(TRANSITIVE));

    assert.equal(findNpmVersion(versions, '@vaadin/react-components'), '25.4.0-alpha1');
    assert.equal(versions.react['react-components-pro'].mode, 'react');
  });

  it('should leave alone the React components the platform declares itself', () => {
    const declared = {
      'react-components': { jsVersion: '25.3.0', mode: 'react', npmName: '@vaadin/react-components' },
    };
    const versions = withReactComponents(versionsWith({}, declared), parseReactComponents(TRANSITIVE));

    assert.equal(versions.react['react-components'].jsVersion, '25.3.0');
  });
});
