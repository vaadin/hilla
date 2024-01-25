import type { RouteMeta } from '../src/collectRoutes.js';

export function createTestingRouteMeta(dir: URL): RouteMeta {
  return {
    path: '',
    layout: undefined,
    children: [
      {
        path: 'about',
        file: new URL('about.tsx', dir),
        children: [],
      },
      {
        path: 'profile',
        layout: undefined,
        children: [
          { path: '', file: new URL('profile/index.tsx', dir), children: [] },
          {
            path: 'account',
            layout: new URL('profile/account/account.layout.tsx', dir),
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
            layout: new URL('profile/friends/friends.layout.tsx', dir),
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
