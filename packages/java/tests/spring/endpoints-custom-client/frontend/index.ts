import { Router } from '@vaadin/router';
import { Flow } from 'Frontend/generated/jar-resources/Flow';

import './main-view';

const { serverSideRoutes } = new Flow({
  imports: async () => import('Frontend/generated/flow/generated-flow-imports.js'),
});

const routes = [
  // for client-side, place routes below (more info https://vaadin.com/docs/v15/flow/typescript/creating-routes.html)
  { component: 'main-view', path: '/' },

  // for server-side, the next magic line sends all unmatched routes:
  ...serverSideRoutes, // IMPORTANT: this must be the last entry in the array
];

export const router = new Router(document.querySelector('#outlet'));
// eslint-disable-next-line @typescript-eslint/no-floating-promises
router.setRoutes(routes);
