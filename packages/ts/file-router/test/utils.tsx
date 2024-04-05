import { appendFile, mkdir, mkdtemp } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { pathToFileURL } from 'node:url';
import type { JSX } from 'react';
import sinon from 'sinon';
import type { Logger } from 'vite';
import type { RouteModule } from '../src/types.js';
import type { RouteMeta } from '../src/vite-plugin/collectRoutesFromFS.js';

export async function createTmpDir(): Promise<URL> {
  return pathToFileURL(`${await mkdtemp(join(tmpdir(), 'file-router-'))}/`);
}

export async function createTestingRouteFiles(dir: URL): Promise<void> {
  // Generates the following directory structure:
  // root
  // ├── profile
  // │   ├── account
  // │   │   ├── layout.tsx
  // │   │   └── security
  // │   │       ├── password.jsx
  // │   │       ├── two-factor-auth.tsx
  // │   │       └── two-factor-auth-ignored.ts
  // │   ├── friends
  // │   │   ├── layout.tsx
  // │   │   ├── list.jsx
  // │   │   ├── list-ignored.js
  // │   │   └── {user}.tsx
  // │   ├── index.tsx
  // │   └── index.css
  // ├── test
  // │   ├── {{optional}}.tsx
  // │   ├── {...wildcard}.tsx
  // │   └── empty.tsx
  // └── nameToReplace.tsx

  await Promise.all([
    mkdir(new URL('profile/account/security/', dir), { recursive: true }),
    mkdir(new URL('profile/friends/', dir), { recursive: true }),
    mkdir(new URL('test', dir), { recursive: true }),
  ]);
  await Promise.all([
    appendFile(
      new URL('profile/account/@layout.tsx', dir),
      "export const config = { title: 'Account' };\nexport default function AccountLayout() {};",
    ),
    appendFile(new URL('profile/account/security/password.jsx', dir), 'export default function Password() {};'),
    appendFile(new URL('profile/account/security/password.scss', dir), ''),
    appendFile(
      new URL('profile/account/security/two-factor-auth.tsx', dir),
      'export default function TwoFactorAuth() {};',
    ),
    appendFile(
      new URL('profile/account/security/two-factor-auth-ignored.ts', dir),
      'export default function TwoFactorAuthIgnored() {};',
    ),
    appendFile(new URL('profile/friends/@layout.tsx', dir), 'export default function FriendsLayout() {};'),
    appendFile(
      new URL('profile/friends/list.jsx', dir),
      "export const config = { title: 'List' };\nexport default function List() {};",
    ),
    appendFile(
      new URL('profile/friends/list-ignored.js', dir),
      "export const config = { title: 'List' };\nexport default function ListIgnored() {};",
    ),
    appendFile(
      new URL('profile/friends/{user}.tsx', dir),
      "export const config = { title: 'User' };\nexport default function User() {};",
    ),
    appendFile(
      new URL('profile/@index.tsx', dir),
      "export const config = { title: 'Profile' };\nexport default function Profile() {};",
    ),
    appendFile(new URL('profile/index.css', dir), ''),
    appendFile(
      new URL('nameToReplace.tsx', dir),
      "export const config = { route: 'about', title: 'About' };\nexport default function About() {};",
    ),
    appendFile(
      new URL('test/{...wildcard}.tsx', dir),
      "export const config = { title: 'Wildcard' };\nexport default function Wildcard() {};",
    ),
    appendFile(
      new URL('test/{{optional}}.tsx', dir),
      "export const config = { title: 'Optional' };\nexport default function Optional() {};",
    ),
    appendFile(new URL('test/empty.tsx', dir), ''),
    appendFile(new URL('test/_ignored.tsx', dir), 'export default function Ignored() {};'),
    appendFile(new URL('test/no-default-export.tsx', dir), 'export const config = { title: "No Default Export" };'),
  ]);
}

export function createTestingRouteMeta(dir: URL): RouteMeta {
  return {
    path: '',
    children: [
      {
        path: 'nameToReplace',
        file: new URL('nameToReplace.tsx', dir),
        children: [],
      },
      {
        path: 'profile',
        children: [
          { path: '', file: new URL('profile/@index.tsx', dir), children: [] },
          {
            path: 'account',
            layout: new URL('profile/account/@layout.tsx', dir),
            children: [
              {
                path: 'security',
                children: [
                  {
                    path: 'password',
                    file: new URL('profile/account/security/password.jsx', dir),
                    children: [],
                  },
                  {
                    path: 'two-factor-auth',
                    file: new URL('profile/account/security/two-factor-auth.tsx', dir),
                    children: [],
                  },
                ],
              },
            ],
          },
          {
            path: 'friends',
            layout: new URL('profile/friends/@layout.tsx', dir),
            children: [
              {
                path: 'list',
                file: new URL('profile/friends/list.jsx', dir),
                children: [],
              },
              {
                path: '{user}',
                file: new URL('profile/friends/{user}.tsx', dir),
                children: [],
              },
            ],
          },
        ],
      },
      {
        path: 'test',
        children: [
          // Ignored route (that has the name `_ignored.tsx` is not included in the route meta.
          // Also empty files or files without default export are not included.
          {
            path: '{{optional}}',
            file: new URL('test/{{optional}}.tsx', dir),
            children: [],
          },
          {
            path: '{...wildcard}',
            file: new URL('test/{...wildcard}.tsx', dir),
            children: [],
          },
        ],
      },
    ],
  };
}

export const components = {
  about: {
    // eslint-disable-next-line func-name-matching
    default: function About(): JSX.Element {
      return <></>;
    },
    config: { title: 'About', route: 'about', menu: { order: 1 } },
  },
  friends: {
    // eslint-disable-next-line func-name-matching
    default: function Friends(): JSX.Element {
      return <></>;
    },
    config: { title: 'Friends' },
  },
  friendsList: {
    // eslint-disable-next-line func-name-matching
    default: function FriendsList(): JSX.Element {
      return <></>;
    },
    config: { title: 'Friends List', route: 'friends-list' },
  },
  friend: {
    // eslint-disable-next-line func-name-matching
    default: function Friend(): JSX.Element {
      return <></>;
    },
    config: { title: 'Friend' },
  },
  hidden: {
    // eslint-disable-next-line func-name-matching
    default: function Hidden(): JSX.Element {
      return <></>;
    },
    config: { menu: { exclude: true } },
  },
  wildcard: {
    // eslint-disable-next-line func-name-matching
    default: function Wildcard(): JSX.Element {
      return <></>;
    },
    config: { title: 'Wildcard' },
  },
  optional: {
    // eslint-disable-next-line func-name-matching
    default: function Optional(): JSX.Element {
      return <></>;
    },
    config: { title: 'Optional' },
  },
  server: {
    // eslint-disable-next-line func-name-matching
    default: function Server(): JSX.Element {
      return <></>;
    },
    config: { title: 'Server' },
  },
} satisfies Record<string, RouteModule>;

export function createLogger(): Logger {
  return {
    info: sinon.stub(),
    warn: sinon.stub(),
    warnOnce: sinon.stub(),
    error: sinon.stub(),
    clearScreen: sinon.stub(),
    hasErrorLogged: sinon.stub(),
    hasWarned: false,
  };
}
