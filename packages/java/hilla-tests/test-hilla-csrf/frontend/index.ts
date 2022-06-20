import { Router, Route } from '@vaadin/router';
import './views/helloworld/hello-world-view';

export const router = new Router(document.querySelector('#outlet'));

export const routes: Route[] = [
  // place routes below (more info https://vaadin.com/docs/latest/fusion/routing/overview)
  {
    path: '',
    component: 'hello-world-view',
  },
  {
    path: 'hello',
    component: 'hello-world-view',
  }
];
router.setRoutes(routes);
