# The build scripts

Hilla ships npm packages and Java artifacts that must agree with the versions
of Flow, the Vaadin platform and the web components they run with. Most of
what is unusual about the build is there to keep those versions in step
without hand edits, and to install npm dependencies once for the whole
repository rather than once per test application. This page says what does
it, where, and why. How exactly is in the scripts.

## `npm run build`

The root `package.json` chains four steps:

| Step            | Runs                                                                    | Writes                                                                                                        |
| --------------- | ----------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------- |
| `build:align`   | `mvn -pl com.vaadin:hilla-scripts`: [`FlowPackageJsonUpdater`][updater] | the root `package.json`                                                                                       |
| `build:prepare` | [`prepare/index.ts`](prepare/index.ts)                                  | `hilla-versions.json` and its copies, the components `package.json` resources, every workspace `package.json` |
| `npm install`   |                                                                         | the root `node_modules` and `package-lock.json`                                                               |
| `build:run`     | Nx, `build` of every `packages/ts/*` in dependency order                | `packages/ts/*` outputs                                                                                       |

`npm install` runs again after the first two steps because they change the
`package.json` files it installs from.

[updater]: java/hilla-scripts/src/main/java/com/vaadin/hilla/scripts/FlowPackageJsonUpdater.java

## Where the versions come from

There are two sources, and each step reads one of them.

**Flow → root `package.json`** (`build:align`). Flow decides the versions of
the npm packages it installs into an application (React, React Router, Vite
and their plugins…), and ships them as `package.json` resources inside its
jars. A small Maven module reads them from the classpath and copies
the matching entries (`default`, `react-router`, `vite`) into the root
`package.json`. It is written in Java because the versions only exist inside
the Flow jars of the `flow.version` in the pom.

**Platform and flow-components → `hilla-versions.json`** (`build:prepare`).
The script downloads, from GitHub, the platform `versions.json` and the
platform's own release generator scripts (run in a sandbox, so that Hilla
writes its versions file and release notes the way the platform does). The
platform no longer lists the web component npm packages, so the script also
reads the `@NpmPackage` annotations and the `REACT_COMPONENTS` map from a few
Java sources in vaadin/flow-components. It writes:

- `packages/java/hilla/hilla-versions.json`, and a copy as
  `vaadin-core-versions.json` in the test applications that need one (listed
  in `destination` in [`prepare/config.ts`](prepare/config.ts));
- the components `package.json` resources in `packages/java/hilla`, through
  which Flow adds the Hilla npm packages to an application;
- the release notes in `prepare/results/`.

Then it writes every version it knows into the root `package.json`, and from
there into every workspace `package.json`, so that the packages and the test
applications declare the same versions as the root.

The branch read from both repositories is `main` unless
`--platform-branch=`/`PLATFORM_BRANCH` says otherwise. flow-components follows
the platform branch, since both name their release branches alike;
`--flow-components-branch=`/`FLOW_COMPONENTS_BRANCH` overrides it. On a
maintenance branch, set the branch, or the build aligns with `main`.

If a package Hilla depends on gets no version, the script fails rather than
leaving it stale. That usually means flow-components moved the annotation:
add the new file to `componentSources` in `prepare/config.ts`.

## Deliberate exceptions

- **`typescript`.** TypeScript 7 is the native compiler only. Hilla's build,
  tooling, and some parts (typescript-eslint, Nx, file router) need the
  JavaScript API, so the root aliases `typescript` to
  `@typescript/typescript6`, and installs TypeScript 7 as `@typescript/native`,
  which owns `tsc`. Flow declares the same today, but the two skips serve
  different purposes: `NOT_ALIGNED` prevents Flow's version from replacing
  the root alias, while `NOT_PROPAGATED` prevents that alias from being copied
  into workspaces, letting test applications keep Flow's plain `typescript`.
- **`.npmrc`.** Alignment changes the versions in `package.json` files whose
  `node_modules` and `package-lock.json` already exist, and the build installs
  over them:
  - `prefer-dedupe` keeps repeated installs from piling up nested duplicate
    copies of the changed packages;
  - `legacy-peer-deps` most likely works around npm failing to resolve peer
    dependencies when related packages change versions together against an
    existing install (ERESOLVE, as in
    [npm/cli#3666](https://github.com/npm/cli/issues/3666));
  - `save-exact` keeps the exact versions alignment wrote.
- **`overrides`** in the root `package.json` force the root's `esbuild` and
  `prettier` on the whole tree.

## One install for the test applications

The test applications in `packages/java/tests` are npm workspaces of the root
(see `workspaces` in the root `package.json`). They depend on the Hilla
packages with `file:` links to `packages/ts/*`. So one `npm install` at the
root, with one `package-lock.json`, installs them all against the packages
being built. Otherwise each application would do its own deep install when
Flow runs `npm install` in it, slowly and with versions of its own.
`build:prepare` deletes the `node_modules` of the test applications so that
none keeps a stale nested install. `endpoints-maven-engine` is left out of
the workspaces: it installs on its own, from its own `package.json`.

Flow still edits the `package.json` of a test application when it runs, so
those files change in CI. [`updateLockFiles.cjs`](updateLockFiles.cjs)
commits such changes and opens the "chore: update package[-lock].json" pull
requests.

## Building the TypeScript packages

Each package's `build` script runs one of two builders:

- [`fast-build.ts`](fast-build.ts), with oxc-transform, file by file, for most
  packages;
- [`build.ts`](build.ts), a full emit with the TypeScript 6 compiler, for
  `lit-form`, `models`, `react-crud` and `react-signals`.

Both replace `__REGISTER__` with the code that registers the package and its
version in `window.Vaadin.registrations` ([`utils/injectRegister.ts`](utils/injectRegister.ts)),
and turn `*.obj.css` into modules that export a `CSSStyleSheet`
([`utils/compileCSS.ts`](utils/compileCSS.ts)). The shared Vitest configs are
in [`vite/`](vite/).

## The Maven side

`mvn install` builds the Java modules, plus two profiles that are on unless
`-Drelease` is set: `it` (the test applications) and `scripts` (the
`hilla-scripts` module of `build:align`). Run `npm run build` first: the
versions files it writes are not in git, and the `hilla` jar and the test
applications include them. The test applications also need the
`node_modules` it installs.

CI (`.github/workflows/validation.yml`) does the same in its `init` job, then
hands the result, including the generated versions files and
`prepare/results/`, to the other jobs as an archive. The setup action deletes
the Vaadin SNAPSHOTs from the restored Maven cache, so that every run uses
the latest Flow.

## Other scripts

- [`link-npm.sh`](link-npm.sh): run in an application to install the Hilla
  npm packages from this checkout.
- [`cherryPick.cjs`](cherryPick.cjs): backports merged pull requests labelled
  `target/<branch>` and labels them with the outcome.
- [`bump/`](bump/): remaps branch protection settings for a new branch.
- [`polyfills.ts`](polyfills.ts): `Array.fromAsync` for `build.ts`, on Node
  versions without it.

## When it breaks

- _"No version found for …"_ from `build:prepare`: flow-components moved an
  annotation; see above.
- _Two packages competing for `tsc`_: `typescript` got aligned with Flow; see
  the exceptions above.
- _Versions out of step with the platform on a release branch_: the build read
  `main`; set `PLATFORM_BRANCH`.
- _Test application fails on a Flow change that already landed_: a stale Flow
  SNAPSHOT in `~/.m2`; delete `~/.m2/repository/com/vaadin/*/*-SNAPSHOT`.
- _`{{version}}` left in a generated file_: the platform's `versions.json`
  changed shape, and the merge in [`prepare/npmPackages.ts`](prepare/npmPackages.ts)
  needs to follow it.
