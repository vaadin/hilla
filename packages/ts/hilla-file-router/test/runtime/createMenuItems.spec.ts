import { expect, use } from '@esm-bundle/chai';
import chaiLike from 'chai-like';
import { createMenuItems } from '../../src/runtime/createMenuItems.js';
import { components, createTestingAgnosticRoutes } from '../utils.js';

use(chaiLike);

describe('@vaadin/hilla-file-router', () => {
  describe('getMenuItems', () => {
    it('should generate a set of menu items', () => {
      const routes = createTestingAgnosticRoutes();
      const items = createMenuItems(routes, {
        server: {
          views: {
            '/server': components.server.config,
          },
        },
      });

      expect(items).to.be.like({
        '/about': components.about.config,
        '/profile/friends/friends-list': components.friendsList.config,
        '/profile/friends/:user': components.friend.config,
        '/server': components.server.config,
      });
    });
  });
});
