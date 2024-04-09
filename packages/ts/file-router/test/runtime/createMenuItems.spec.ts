import { expect, use } from '@esm-bundle/chai';
import chaiLike from 'chai-like';
import type { Writable } from 'type-fest';
import { createMenuItems } from '../../src/runtime/createMenuItems.js';
import { RouteParamType } from '../../src/shared/routeParamType.js';
import type { MenuItem } from '../../src/types.js';

use(chaiLike);

const collator = new Intl.Collator('en-US');

function cleanup(items: Array<Writable<MenuItem>>) {
  items
    .sort(({ to: a }, { to: b }) => collator.compare(a, b))
    .forEach((item) => {
      if (!item.title) {
        delete item.title;
      }

      if (!item.to) {
        delete item.icon;
      }
    });
}

describe('@vaadin/hilla-file-router', () => {
  describe('createMenuItems', () => {
    it('should generate a set of menu items', () => {
      const items = createMenuItems({
        server: {
          views: {
            '/about': { route: 'about', title: 'About' },
            '/profile/': { title: 'Profile' },
            '/profile/account/security/password': { title: 'Password' },
            '/profile/account/security/two-factor-auth': { title: 'Two Factor Auth' },
            '/profile/friends/list': { title: 'List' },
            '/profile/friends/:user': { title: 'User', params: { ':user': RouteParamType.Required } },
            '/test/empty': {},
            '/test/:optional?': { title: 'Optional', params: { ':optional?': RouteParamType.Optional } },
            '/test/*': { title: 'Wildcard', params: { '*': RouteParamType.Wildcard } },
            '/test/no-default-export': { title: 'No Default Export' },
          },
        },
      });
      cleanup(items as Array<Writable<MenuItem>>);

      const expected = [
        {
          title: 'About',
          to: '/about',
        },
        {
          title: 'Profile',
          to: '/profile/',
        },
        {
          title: 'Password',
          to: '/profile/account/security/password',
        },
        {
          title: 'Two Factor Auth',
          to: '/profile/account/security/two-factor-auth',
        },
        {
          title: 'List',
          to: '/profile/friends/list',
        },
        {
          title: 'Optional',
          to: '/test/',
        },
        {
          to: '/test/empty',
        },
        {
          title: 'Wildcard',
          to: '/test/',
        },
        {
          title: 'No Default Export',
          to: '/test/no-default-export',
        },
      ];
      cleanup(expected);

      expect(items).to.be.like(expected);
    });
  });
});
