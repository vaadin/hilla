import { AppLayout } from '@hilla/react-components/AppLayout.js';
import { DrawerToggle } from '@hilla/react-components/DrawerToggle.js';
import { Scroller } from '@hilla/react-components/Scroller.js';
import { routes } from 'Frontend/routes.js';
import { NavLink, Outlet } from 'react-router-dom';

export default function MainLayout() {
  const menuRoutes = routes[0]!.children;

  return (
    <AppLayout className="block h-full" primarySection="drawer">
      <header slot="drawer">
        <h1 className="text-l m-0">My App</h1>
      </header>
      <Scroller slot="drawer" scroll-direction="vertical">
        <nav>
          <ul>
            {menuRoutes.map(({ path }) => (
              <li key={path}>
                <NavLink to={path}>{path}</NavLink>
              </li>
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
