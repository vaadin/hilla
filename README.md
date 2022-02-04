<a target="_blank" href="https://hilla.dev"><img src="https://discord.com/assets/e4923594e694a21542a489471ecffa50.svg" width="100" alt="Join the discussion in Hilla Discord"></img></a>

![Frontend CI](https://github.com/vaadin/hilla/actions/workflows/ts.yml/badge.svg)
![Java CI](https://github.com/vaadin/hilla/actions/workflows/java.yml/badge.svg)
[![codecov](https://codecov.io/gh/vaadin/hilla/branch/main/graph/badge.svg?token=PQMTMS8ECC)](https://codecov.io/gh/vaadin/hilla)

Hilla
======
*[Hilla](https://hilla.dev) is a TypeScript and Java web framework for building modern web applications. You can create UIs in TypeScript and connect to any backend through endpoints written in Java.*

**For instructions about developing web applications with Hilla**, please refer to our [documentation site](https://vaadin.com/docs/latest/fusion/overview).

Join the Hilla community chat in https://hilla.dev

## TypeScript

The collection of frontend and NodeJS utilities used by Hilla.

| Package                 | Status |
|-------------------------|--------|
| [@hilla/form](./packages/ts/form) | [![Latest Stable Version](https://img.shields.io/npm/v/@hilla/form.svg)](https://www.npmjs.com/package/@hilla/form) |
| [@hilla/frontend](./packages/ts/hilla-frontend) | [![Latest Stable Version](https://img.shields.io/npm/v/@hilla/frontend.svg)](https://www.npmjs.com/package/@hilla/frontend) |

### Contribution

You can download the project and run tests using the following commands:
```bash
$ git clone https://github.com/vaadin/hilla.git
$ npm install
$ npm run build
$ npm test
```

### Requirements

To work with this project as a developer, you need the following versions of `node` and `npm`:

- **NodeJS**: `>= 16.13.0` (native support for ES Modules and NodeJS execution of the newest hooks),
- **npm**: `^7` (`package-lock.json` is of version 2; also, `lerna` is unable to bootstrap this project correctly with lower `npm`)
