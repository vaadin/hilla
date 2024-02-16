import { expect, use } from '@esm-bundle/chai';
import chaiLike from 'chai-like';
import { createMenuItems } from '../../src/runtime/createMenuItems.js';
import { createTestingViewMap } from '../utils.js';

use(chaiLike);

describe('@vaadin/hilla-file-router', () => {
  describe('getMenuItems', () => {
    it('should generate a set of menu items', () => {
      const items = createMenuItems({
        server: {
          views: createTestingViewMap(),
        },
      });

      expect(items).to.be.like([
        {
          icon: undefined,
          title: 'About',
          to: '/about',
        },
        {
          icon: undefined,
          title: 'Profile',
          to: '/profile/',
        },
        {
          icon: undefined,
          title: 'Password',
          to: '/profile/account/security/password',
        },
        {
          icon: undefined,
          title: 'Two Factor Auth',
          to: '/profile/account/security/two-factor-auth',
        },
        {
          icon: undefined,
          title: 'List',
          to: '/profile/friends/list',
        },
        {
          icon: undefined,
          title: 'Optional',
          to: '/test/',
        },
        {
          icon: undefined,
          title: 'Wildcard',
          to: '/test/',
        },
      ]);
    });
  });
});
