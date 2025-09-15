import { Router } from '@vaadin/router';

import './main-view';

const routes = [{ component: 'main-view', path: '/' }];

export const router = new Router(document.querySelector('#outlet'));
// eslint-disable-next-line @typescript-eslint/no-floating-promises
router.setRoutes(routes);
