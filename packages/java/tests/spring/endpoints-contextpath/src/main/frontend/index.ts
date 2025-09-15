import './test-view.js';
import { Route, Router } from '@vaadin/router';

const routes: Route[] = [{ path: '/', component: 'test-view' }];

// Vaadin router needs an outlet in the index.html page to display views
const router = new Router(document.querySelector('#outlet'));
router.setRoutes(routes);
