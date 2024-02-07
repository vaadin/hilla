import { expect, use } from '@esm-bundle/chai';
import chaiLike from 'chai-like';
import type { JSX } from 'react';
import type { AgnosticRoute } from '../src/runtime/utils.js';
import { type RouteModule, toReactRouter } from '../src/runtime.js';

use(chaiLike);

describe('@vaadin/hilla-file-router', () => {
  describe('react', () => {
    function About(): JSX.Element {
      return <></>;
    }

    About.config = { title: 'About' };

    function Friends(): JSX.Element {
      return <></>;
    }

    Friends.config = { title: 'Friends' };

    function FriendsList(): JSX.Element {
      return <></>;
    }

    FriendsList.config = { title: 'Friends List' };

    function Friend(): JSX.Element {
      return <></>;
    }

    Friend.config = { title: 'Friend' };

    it('should be able to convert an agnostic routes to React Router routes', () => {
      const routes: AgnosticRoute<RouteModule> = {
        path: '',
        children: [
          {
            path: 'about',
            module: {
              default: About,
              config: About.config,
            },
          },
          {
            path: 'profile',
            children: [
              {
                path: 'friends',
                module: {
                  default: Friends,
                  config: Friends.config,
                },
                children: [
                  {
                    path: 'list',
                    module: {
                      default: FriendsList,
                      config: FriendsList.config,
                    },
                  },
                  {
                    path: '{user}',
                    module: {
                      default: Friend,
                      config: Friend.config,
                    },
                  },
                ],
              },
            ],
          },
        ],
      };

      const result = toReactRouter(routes);

      expect(result).to.be.like({
        path: '',
        children: [
          {
            path: 'about',
            element: <About />,
            handle: About.config,
          },
          {
            path: 'profile',
            children: [
              {
                path: 'friends',
                element: <Friends />,
                handle: Friends.config,
                children: [
                  {
                    path: 'list',
                    element: <FriendsList />,
                    handle: FriendsList.config,
                  },
                  {
                    path: '{user}',
                    element: <Friend />,
                    handle: Friend.config,
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
