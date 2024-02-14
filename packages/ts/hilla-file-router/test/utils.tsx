import { appendFile, mkdir, mkdtemp } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { pathToFileURL } from 'node:url';
import type { JSX } from 'react';
import type { AgnosticRoute, ViewConfig } from '../runtime/utils.js';
import type { RouteModule } from '../runtime.js';
import type { RouteMeta } from '../src/vite-plugin/collectRoutesFromFS.js';

export async function createTmpDir(): Promise<URL> {
  return pathToFileURL(`${await mkdtemp(join(tmpdir(), 'hilla-file-router-'))}/`);
}

export async function createTestingRouteFiles(dir: URL): Promise<void> {
  await Promise.all([
    mkdir(new URL('profile/account/security/', dir), { recursive: true }),
    mkdir(new URL('profile/friends/', dir), { recursive: true }),
  ]);
  await Promise.all([
    appendFile(
      new URL('profile/account/$layout.tsx', dir),
      "export const config = { title: 'Account' };\nexport default function AccountLayout() {};",
    ),
    appendFile(new URL('profile/account/security/password.jsx', dir), 'export default function Password() {};'),
    appendFile(new URL('profile/account/security/password.scss', dir), ''),
    appendFile(
      new URL('profile/account/security/two-factor-auth.ts', dir),
      'export default function TwoFactorAuth() {};',
    ),
    appendFile(new URL('profile/friends/$layout.tsx', dir), 'export default function FriendsLayout() {};'),
    appendFile(
      new URL('profile/friends/list.js', dir),
      "export const config = { title: 'List' };\nexport default function List() {};",
    ),
    appendFile(
      new URL('profile/friends/{user}.tsx', dir),
      "export const config = { title: 'User' };\nexport default function User() {};",
    ),
    appendFile(
      new URL('profile/$index.tsx', dir),
      "export const config = { title: 'Profile' };\nexport default function Profile() {};",
    ),
    appendFile(new URL('profile/index.css', dir), ''),
    appendFile(
      new URL('nameToReplace.tsx', dir),
      "export const config = { route: 'about', title: 'About' };\nexport default function About() {};",
    ),
  ]);
}

export function createTestingRouteMeta(dir: URL): RouteMeta {
  return {
    path: '',
    layout: undefined,
    children: [
      {
        path: 'nameToReplace',
        file: new URL('nameToReplace.tsx', dir),
        children: [],
      },
      {
        path: 'profile',
        layout: undefined,
        children: [
          { path: '', file: new URL('profile/$index.tsx', dir), children: [] },
          {
            path: 'account',
            layout: new URL('profile/account/$layout.tsx', dir),
            children: [
              {
                path: 'security',
                layout: undefined,
                children: [
                  {
                    path: 'password',
                    file: new URL('profile/account/security/password.jsx', dir),
                    children: [],
                  },
                  {
                    path: 'two-factor-auth',
                    file: new URL('profile/account/security/two-factor-auth.ts', dir),
                    children: [],
                  },
                ],
              },
            ],
          },
          {
            path: 'friends',
            layout: new URL('profile/friends/$layout.tsx', dir),
            children: [
              {
                path: 'list',
                file: new URL('profile/friends/list.js', dir),
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
    ],
  };
}

export const components = {
  about: {
    // eslint-disable-next-line func-name-matching
    default: function About(): JSX.Element {
      return <></>;
    },
    config: { title: 'About', menu: { order: 1 } },
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
  server: {
    // eslint-disable-next-line func-name-matching
    default: function Server(): JSX.Element {
      return <></>;
    },
    config: { title: 'Server' },
  },
} satisfies Record<string, RouteModule>;

export function createTestingAgnosticRoutes(): AgnosticRoute<RouteModule> {
  return {
    path: '',
    children: [
      {
        path: 'about',
        module: components.about,
      },
      {
        path: 'hidden',
        module: components.hidden,
      },
      {
        path: 'profile',
        children: [
          {
            path: 'friends',
            module: components.friends,
            children: [
              {
                path: 'list',
                module: components.friendsList,
              },
              {
                path: ':user',
                module: components.friend,
              },
            ],
          },
        ],
      },
    ],
  };
}
