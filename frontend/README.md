# Vaadin Frontend

![Frontend CI](https://github.com/vaadin/fusion/actions/workflows/frontend.yml/badge.svg)
[![codecov](https://codecov.io/gh/vaadin/fusion/branch/main/graph/badge.svg?token=PQMTMS8ECC)](https://codecov.io/gh/vaadin/fusion)

The collection of frontend utilities used by Vaadin Flow and Fusion.

| Package                 | Status |
|-------------------------|--------|
| [@vaadin/common-frontend](./packages/common-frontend) | [![Latest Stable Version](https://img.shields.io/npm/v/@vaadin/common-frontend.svg)](https://www.npmjs.com/package/@vaadin/common-frontend) |
| [@vaadin/fusion-frontend](./packages/fusion-frontend) | [![Latest Stable Version](https://img.shields.io/npm/v/@vaadin/fusion-frontend.svg)](https://www.npmjs.com/package/@vaadin/fusion-frontend) |

## Contribution

You can download the project and run tests using the following commands:
```bash
$ git clone https://github.com/vaadin/fusion.git
$ cd fusion
$ npm install
$ npm run build
$ npm test
```

## Requirements

To work with this project as a developer, you need the following versions of `node` and `npm`:

- **NodeJS**: `^12.20.0 || ^14.13.1 || >=16.0.0` (native support for ES Modules),
- **npm**: `^7` (`package-lock.json` is of version 2; also, `lerna` is unable to bootstrap this project correctly with lower `npm`)



