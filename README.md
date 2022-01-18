<a target="_blank" href="https://vaad.in/fusion"><img src="https://discord.com/assets/e4923594e694a21542a489471ecffa50.svg" width="100" alt="Join the discussion in Hilla Discord"></img></a>

![Frontend CI](https://github.com/vaadin/fusion/actions/workflows/frontend.yml/badge.svg)
[![codecov](https://codecov.io/gh/vaadin/fusion/branch/main/graph/badge.svg?token=PQMTMS8ECC)](https://codecov.io/gh/vaadin/fusion)

Hilla
======
*[Hilla](https://vaadin.com/fusion) is a TypeScript and Java web framework for building modern web applications. You can create UIs in TypeScript and connect to any backend through endpoints written in Java.*

**For instructions about developing web applications with Hilla**, please refer to our [documentation site](https://vaadin.com/docs/latest/fusion/overview).

Join the Hilla community chat in https://vaad.in/fusion

**Note**: Currently the code of Hilla is hosted at https://github.com/vaadin/flow together with the Flow framework. The Hilla-only code will be ported to this repository in the near future. Tickets can already be created in this repository.

## TypeScript

The collection of frontend and NodeJS utilities used by Hilla.

| Package                 | Status |
|-------------------------|--------|
| [@vaadin/form](./packages/ts/form) | [![Latest Stable Version](https://img.shields.io/npm/v/@vaadin/form.svg)](https://www.npmjs.com/package/@vaadin/form) |
| [@vaadin/fusion-frontend](./packages/ts/fusion-frontend) | [![Latest Stable Version](https://img.shields.io/npm/v/@vaadin/fusion-frontend.svg)](https://www.npmjs.com/package/@vaadin/fusion-frontend) |

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
