import './vaadinGlobals.js'; // eslint-disable-line import/no-unassigned-import
import { expect, describe, it } from 'vitest';
import { createMenuItems, viewsSignal } from '../../src/runtime/createMenuItems.js';
import { deepRemoveNullProps } from '../utils.js';

const collator = new Intl.Collator('en-US');

describe('@vaadin/hilla-file-router', () => {
  describe('createMenuItems', () => {
    it('should generate a set of menu items', () => {
      viewsSignal.value = {
        '/about': { route: 'about', title: 'About' },
        '/profile/': { title: 'Profile' },
        '/profile/account/security/password': { title: 'Password' },
        '/profile/account/security/two-factor-auth': { title: 'Two Factor Auth' },
        '/profile/friends/list': { title: 'List' },
        '/test/empty': {},
        '/test/no-default-export': { title: 'No Default Export' },
      };
      const items = createMenuItems()
        .slice()
        .sort(({ to: a }, { to: b }) => collator.compare(a, b));

      expect(deepRemoveNullProps(items)).to.be.deep.equal([
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
          to: '/test/empty',
        },
        {
          title: 'No Default Export',
          to: '/test/no-default-export',
        },
      ]);
    });

    it('should sort menu items by order then by natural string comparison based on path', () => {
      viewsSignal.value = {
        '/a/b': { route: 'about', title: 'About' },
        '': { title: 'Profile' },
        '/profile/account/security/password': { title: 'Password', menu: { order: 20 } },
        '/profile/account/security/two-factor-auth': { title: 'Two Factor Auth', menu: { order: 20 } },
        '/b': { title: 'List' },
        '/': { title: 'Root' },
        '/test/empty': { title: 'empty', menu: { order: 5 } },
        '/test/no-default-export': { title: 'No Default Export', menu: { order: 10 } },
      };

      expect(deepRemoveNullProps(createMenuItems())).to.be.deep.equal([
        {
          order: 5,
          title: 'empty',
          to: '/test/empty',
        },
        {
          order: 10,
          title: 'No Default Export',
          to: '/test/no-default-export',
        },
        {
          order: 20,
          title: 'Password',
          to: '/profile/account/security/password',
        },
        {
          order: 20,
          title: 'Two Factor Auth',
          to: '/profile/account/security/two-factor-auth',
        },
        {
          title: 'Profile',
          to: '',
        },
        {
          title: 'Root',
          to: '/',
        },
        {
          title: 'About',
          to: '/a/b',
        },
        {
          title: 'List',
          to: '/b',
        },
      ]);
    });

    it('should exclude items with { "menu": { "exclude": true } } but leave children in place', () => {
      viewsSignal.value = {
        '/foo': { title: 'Foo' },
        '/bar': { title: 'Bar', menu: { exclude: true } }, // should be excluded
        '/bar/foo': { title: 'Bar Foo' },
        '/baz': { title: 'Baz' },
        '/baz/bar': { title: 'Baz Bar' },
        '/baz/bar/foo': { title: 'Baz Bar Foo', menu: { exclude: true } }, // should be excluded
        '/baz/bar/foo/buzz': { title: 'Baz Bar Foo Buzz' },
      };

      expect(deepRemoveNullProps(createMenuItems())).to.be.deep.equal([
        {
          title: 'Bar Foo',
          to: '/bar/foo',
        },
        {
          title: 'Baz',
          to: '/baz',
        },
        {
          title: 'Baz Bar',
          to: '/baz/bar',
        },
        {
          title: 'Baz Bar Foo Buzz',
          to: '/baz/bar/foo/buzz',
        },
        {
          title: 'Foo',
          to: '/foo',
        },
      ]);
    });

    it('should exclude items with variable path segments and their children', () => {
      viewsSignal.value = {
        '/foo': { title: 'Foo' },
        '/bar/:id': { title: 'Bar' }, // should be excluded
        '/bar/:id/foo': { title: 'Bar Foo' }, // should be excluded
        '/baz': { title: 'Baz' },
        '/baz/foo': { title: 'Baz Foo' },
        '/baz/bar/:id': { title: 'Baz Bar' }, // should be excluded
        '/baz/bar/:id/foo': { title: 'Baz Bar Foo' }, // should be excluded
      };

      expect(deepRemoveNullProps(createMenuItems())).to.be.deep.equal([
        {
          title: 'Baz',
          to: '/baz',
        },
        {
          title: 'Baz Foo',
          to: '/baz/foo',
        },
        {
          title: 'Foo',
          to: '/foo',
        },
      ]);
    });
  });
});
