import { expect, use } from '@esm-bundle/chai';
import chaiLike from 'chai-like';
import { type ComponentType, createElement } from 'react';
import { toReactRouter } from '../../src/runtime/toReactRouter.js';
import type { AgnosticRoute } from '../../src/types.js';
import { components, createTestingAgnosticRoutes } from '../utils.js';

use(chaiLike);

describe('@vaadin/hilla-file-router', () => {
  describe('toReactRouter', () => {
    it('should be able to convert an agnostic routes to React Router routes', () => {
      const routes: AgnosticRoute<ComponentType> = createTestingAgnosticRoutes();
      const result = toReactRouter(routes);

      expect(result).to.be.like({
        path: '',
        children: [
          {
            path: 'about',
            element: createElement(components.about.default),
            handle: components.about.config,
          },
          {
            path: 'profile',
            children: [
              {
                path: 'friends',
                element: createElement(components.friends.default),
                handle: components.friends.config,
                children: [
                  {
                    path: 'friends-list',
                    element: createElement(components.friendsList.default),
                    handle: components.friendsList.config,
                  },
                  {
                    path: '{user}',
                    element: createElement(components.friend.default),
                    handle: components.friend.config,
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
