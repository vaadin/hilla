import { Router } from '@vaadin/router';
import './src/test-component.js';
import './src/test-flux.js';
import './src/test-type-script.js';

const routes = [
  { path: 'flux', component: 'test-flux' },
  { path: 'type-script', component: 'test-type-script' },
  { path: '', component: 'test-component' },
  { path: 'login', component: 'test-component' },
];

// Vaadin router needs an outlet in the index.html page to display views
const router = new Router(document.querySelector('#outlet'));
router.setRoutes(routes);
