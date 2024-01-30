import { expect, use } from '@esm-bundle/chai';
import chaiLike from 'chai-like';
import type { JSX } from 'react';
import { type RouteModule, toReactRouter } from '../src/react.js';
import type { AgnosticRoute } from '../utils.js';

use(chaiLike);

describe('@vaadin/hilla-file-router', () => {
  describe('react', () => {
    function About(): JSX.Element {
      return <></>;
    }

    About.meta = { title: 'About' };

    function Friends(): JSX.Element {
      return <></>;
    }

    Friends.meta = { title: 'Friends' };

    function FriendsList(): JSX.Element {
      return <></>;
    }

    FriendsList.meta = { title: 'Friends List' };

    function Friend(): JSX.Element {
      return <></>;
    }

    Friend.meta = { title: 'Friend' };

    it('should be able to convert an agnostic routes to React Router routes', () => {
      const routes = {
        path: '',
        children: [
          {
            path: 'about',
            component: About,
          },
          {
            path: 'profile',
            children: [
              {
                path: 'friends',
                component: Friends,
                children: [
                  {
                    path: 'list',
                    component: FriendsList,
                  },
                  {
                    path: '{user}',
                    component: Friend,
                  },
                ],
              },
            ],
          },
        ],
      } satisfies AgnosticRoute<RouteModule>;

      const result = toReactRouter(routes);

      expect(result).to.be.like({
        path: '',
        children: [
          {
            path: 'about',
            element: <About />,
            handle: About.meta,
          },
          {
            path: 'profile',
            children: [
              {
                path: 'friends',
                element: <Friends />,
                handle: Friends.meta,
                children: [
                  {
                    path: 'list',
                    element: <FriendsList />,
                    handle: FriendsList.meta,
                  },
                  {
                    path: '{user}',
                    element: <Friend />,
                    handle: Friend.meta,
                  },
                ],
              },
            ],
          },
        ],
      });
    });
  });
});
