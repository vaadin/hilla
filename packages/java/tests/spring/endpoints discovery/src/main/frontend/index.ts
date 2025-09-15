// This file adds a fake hilla view to force Flow to detect Hilla properly and
// run the endpoint generator node tasks
import { Router } from '@vaadin/router';

const router = new Router(document.querySelector('#outlet'));

router.setRoutes([
  {
    path: '',
    component: 'hilla-view',
  }
]);
