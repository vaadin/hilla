import { createMenuItems } from '@vaadin/hilla-file-router/runtime.js';
import { i18n, I18nKey, translate } from '@vaadin/hilla-react-i18n';
import { effect } from '@vaadin/hilla-react-signals';
import { SideNavItem } from '@vaadin/react-components';
import { AppLayout } from '@vaadin/react-components/AppLayout.js';
import { DrawerToggle } from '@vaadin/react-components/DrawerToggle.js';
import { Scroller } from '@vaadin/react-components/Scroller.js';
import { Outlet } from 'react-router';
import { Detail } from './detail';

effect(() => {
  i18n.configure();
});

export default function MainLayout() {
  return (
    <AppLayout className="block h-full" primarySection="drawer">
      <header slot="drawer">
        <h1 className="text-l m-0">My App</h1>
      </header>
      <Scroller slot="drawer" scroll-direction="vertical">
        <nav>
          <ul>
            {createMenuItems<Detail>().map(({ to, title, detail }) => (
              <SideNavItem key={to} path={to} title={translate(detail?.description as I18nKey)}>
                {translate(title as I18nKey)}
              </SideNavItem>
            ))}
          </ul>
        </nav>
      </Scroller>
      <footer slot="drawer" />

      <DrawerToggle slot="navbar" aria-label="Menu toggle"></DrawerToggle>
      <h2 slot="navbar" className="text-l m-0"></h2>

      <Outlet />
    </AppLayout>
  );
}
