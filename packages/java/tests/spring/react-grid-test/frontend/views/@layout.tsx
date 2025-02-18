import { createMenuItems } from '@vaadin/hilla-file-router/runtime.js';
import { AppLayout, Icon, SideNav, SideNavItem } from '@vaadin/react-components';
import { DrawerToggle } from '@vaadin/react-components/DrawerToggle.js';
import { Outlet, useLocation, useNavigate } from 'react-router';

export default function MainLayout(): React.JSX.Element {
  const navigate = useNavigate();
  const location = useLocation();
  return (
    <AppLayout className="block h-full" primarySection="drawer">
      <div slot="drawer" className="flex flex-col justify-between h-full p-m">
        <header className="flex flex-col gap-m">
          <h1 className="text-l m-0">Test</h1>
          <SideNav onNavigate={({ path }) => navigate(path!)} location={location}>
            {createMenuItems().map(({ to, title, icon }) => (
              <SideNavItem path={to} key={to}>
                {icon ? <Icon src={icon} slot="prefix"></Icon> : <></>}
                {title}
              </SideNavItem>
            ))}
          </SideNav>
        </header>
      </div>

      <footer slot="drawer" />

      <DrawerToggle slot="navbar" aria-label="Menu toggle"></DrawerToggle>
      <h2 slot="navbar" className="text-l m-0"></h2>

      <Outlet />
    </AppLayout>
  );
}
