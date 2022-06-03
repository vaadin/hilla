import { Router, RouterLocation } from '@vaadin/router';
import { routes } from './routes';
import { appStore } from './stores/app-store';

export const router = new Router(document.querySelector('#outlet'));
appStore.fetchUserInfo().finally(() => {
  // Ensure router access checks are not done before we know if we are logged in
  router.setRoutes(routes);
});

window.addEventListener('vaadin-router-location-changed', (e) => {
  const location: RouterLocation = (e as CustomEvent).detail.location;
  appStore.setLocation(location);
  const title = appStore.currentViewTitle;
  if (title) {
    document.title = title + ' | ' + appStore.applicationName;
  } else {
    document.title = appStore.applicationName;
  }
});
