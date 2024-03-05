import { expect, use } from '@esm-bundle/chai';
import chaiLike from 'chai-like';
import type { Writable } from 'type-fest';
import type { MenuItem } from '../../src/runtime/createMenuItems.js';
import { createMenuItems } from '../../src/runtime/createMenuItems.js';
import { createTestingViewMap } from '../utils.js';

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
  describe('getMenuItems', () => {
    it('should generate a set of menu items', () => {
      const items = createMenuItems({
        server: {
          views: createTestingViewMap(),
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
