import { Commands, Context, Route } from '@vaadin/router';
import { logout, isAuthenticated } from './auth';
import { appStore } from './stores/app-store';
import './views/main-view';

export type ViewRoute = Route & { title?: string; children?: ViewRoute[] };

export const views: ViewRoute[] = [
  // for client-side, place routes below (more info https://vaadin.com/docs/v19/flow/typescript/creating-routes.html)
  {
    path: '',
    component: 'public-view',
    name: 'public',
    title: 'Public',
    action: async () => {
      await import('./views/public/public-view');
    },
  },
  {
    path: 'proxied-service',
    component: 'proxied-service-test-view',
    title: 'Proxied Service Test View',
    action: async () => {
      await import('./views/public/proxied-service-test-view');
    },
  },
  {
    path: 'form',
    component: 'vaadin-elements-view',
    name: 'form',
    title: 'Fusion Form',
    action: async () => {
      await import('./views/public/vaadin-elements-view');
    },
  },
  {
    path: 'private',
    component: 'private-view',
    title: 'Private',
    action: async (_context: Context, commands: Commands) => {
      if (!isAuthenticated()) {
        return commands.redirect('/login');
      }
      await import('./views/private/private-view');
      return undefined;
    },
  },
  {
    path: 'admin',
    component: 'admin-view',
    title: 'Admin View',
    action: async (_context: Context, commands: Commands) => {
      if (!appStore.isUserInRole('admin')) {
        return commands.redirect('/login');
      }
      await import('./views/admin/admin-view');
      return undefined;
    },
  },
];
export const routes: ViewRoute[] = [
  {
    path: '/login',
    component: 'login-view',
    action: async () => {
      await import('./views/login-view');
    },
  },
  {
    path: '/logout',
    action: async (_: Context, commands: Commands) => {
      await logout();
      return commands.redirect('/');
    },
  },
  {
    path: '',
    component: 'main-view',
    children: [...views],
  },
];
