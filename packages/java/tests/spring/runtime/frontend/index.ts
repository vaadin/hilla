import './test-view.js';
import './about-view.js';
import { Route, Router } from '@vaadin/router';

const routes: Route[] = [{ path: '/', component: 'test-view' },
    { path: '/about-view', component: 'about-view' }];

// Vaadin router needs an outlet in the index.html page to display views
const router = new Router(document.querySelector('#outlet'));
router.setRoutes(routes);
