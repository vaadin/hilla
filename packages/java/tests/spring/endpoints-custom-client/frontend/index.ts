import { Flow } from 'Frontend/generated/jar-resources/Flow';
import { Router } from '@vaadin/router';

import './main-view';

const { serverSideRoutes } = new Flow({
  imports: () => import('../target/frontend/generated-flow-imports'),
});

const routes = [
  // for client-side, place routes below (more info https://vaadin.com/docs/v15/flow/typescript/creating-routes.html)
  { path: '/', component: 'main-view' },

  // for server-side, the next magic line sends all unmatched routes:
  ...serverSideRoutes, // IMPORTANT: this must be the last entry in the array
];

export const router = new Router(document.querySelector('#outlet'));
router.setRoutes(routes);
