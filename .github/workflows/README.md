# GitHub Workflows

## Pull request snapshots

`pr-snapshot.yml` builds and publishes a Hilla snapshot for a single pull
request, so a change can be tried out in an application before it is merged.
It is adopted from the workflow of the same name in
[vaadin/flow](https://github.com/vaadin/flow/blob/main/.github/workflows/pr-snapshot.yml).

The build is opt-in per pull request: add the `snapshot build` label and it
starts, and every commit pushed while the label is there republishes the
snapshot. Removing the label stops that.

A snapshot has two halves that have to match:

- The TypeScript packages in `packages/ts` are published as
  [pkg.pr.new](https://pkg.pr.new) previews of the head commit, such as
  `https://pkg.pr.new/vaadin/hilla/@vaadin/hilla-frontend@<full commit sha>`.
  They are published in one call, so their dependencies on each other point at
  the previews of the same commit.
- The Java artifacts are deployed to the Maven snapshot repository. The
  `hilla` jar lists the TypeScript packages Flow adds to an application, and
  the snapshot lists them at the URLs of those previews instead of at the
  version in `packages/ts`, which nobody published.

The Maven version comes from the branch name — everything up to the last slash
is dropped, so `fix/my-thing` on a `25.4-SNAPSHOT` branch publishes
`25.4.my-thing-SNAPSHOT`. That is the scheme the feature branch snapshot builds
of the other Vaadin repositories use, and the workflow uses it to find their
snapshots of the same branch name:

- When the Maven snapshot repository holds a Flow snapshot of that version
  (`flow-bom` `25.4.my-thing-SNAPSHOT` for a branch that builds against Flow
  `25.4-SNAPSHOT`), the snapshot is built against it instead of the Flow
  version of the branch.
- When it holds a Flow Components snapshot of that version, the workflow reads
  the npm versions of the components from the vaadin/flow-components branch
  with exactly the same name as this one, if there is such a branch. A Flow
  Components branch that pins npm versions nobody published fails the
  TypeScript build here as well.

The workflow comments the version on the pull request, with a copy-pasteable
`hilla-bom` import, the `flow-bom` and `flow-components-bom` imports of the
snapshots it was paired with, and the matching `vaadin-bom` import
(`25.4-SNAPSHOT` for the branch above) last, in the order Maven needs for the
snapshots to win. It edits the same comment on every rebuild.

Flow writes the preview URLs to `package.json` of a new application. An
existing application keeps the `@vaadin/hilla-*` versions it already has there,
so those entries have to be removed to get the previews.

Dropping the prefix means the version is only as unique as the part of the
branch name after the last slash: `fix/npe` and `issues/npe` both publish
`25.4.npe-SNAPSHOT`, and the later build replaces the earlier one without
warning.

Publishing needs the credentials, and GitHub only hands secrets to pull
requests from a branch of this repository. That is also what limits who can
trigger a snapshot: pushing such a branch takes write access. Labeling a pull
request from a fork does nothing.

Configuration:

| Name | Kind | Purpose |
|---|---|---|
| `SNAPSHOT_USERNAME` | organization secret | User the snapshot is deployed as, shared with the other builds that publish. |
| `SNAPSHOT_PASSWORD` | organization secret | Its password or token. |
| `MAVEN_SNAPSHOT_DEPLOY_URL` | variable | Address the snapshot is uploaded to, which is the deploy endpoint of the repository rather than `MAVEN_SNAPSHOT_READ_URL` below. Pointing this at the read address is refused with a 403. |
| `MAVEN_SNAPSHOT_READ_URL` | variable (optional) | Address the pull request comment tells people to resolve from, and the one the Flow and Flow Components snapshots are looked up in. Defaults to `https://maven.vaadin.com/vaadin-prereleases`. |

The [pkg.pr.new GitHub App](https://github.com/apps/pkg-pr-new) has to be
installed for this repository, or the npm publish fails. The `snapshot build`
label has to exist in the repository for it to be selectable.
