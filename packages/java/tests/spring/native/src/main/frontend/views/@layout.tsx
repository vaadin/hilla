import { createMenuItems } from '@vaadin/hilla-file-router/runtime.js';
import { i18n } from '@vaadin/hilla-react-i18n';
import { AppLayout, Button, SideNav, SideNavItem } from '@vaadin/react-components';
import { DrawerToggle } from '@vaadin/react-components/DrawerToggle.js';
import { Outlet, useLocation, useNavigate } from 'react-router';
import { useAuth } from 'Frontend/util/auth.js';

// Loads the translations from the classpath through the i18n endpoint. In a
// native image this only works when the translation files are registered as
// resources of the image.
await i18n.configure();

export default function MainLayout(): React.JSX.Element {
  const navigate = useNavigate();
  const location = useLocation();
  const { state, logout } = useAuth();

  return (
    <AppLayout className="block h-full" primarySection="drawer">
      <div slot="drawer" className="flex flex-col justify-between h-full p-m">
        <header className="flex flex-col gap-m">
          <h1 className="text-l m-0">Hilla native ITs</h1>
          <SideNav onNavigate={({ path }) => navigate(path!)} location={location}>
            {createMenuItems().map(({ to, title }) => (
              <SideNavItem path={to} key={to}>
                {title}
              </SideNavItem>
            ))}
          </SideNav>
        </header>
      </div>

      <footer slot="drawer" />

      <DrawerToggle slot="navbar" aria-label="Menu toggle"></DrawerToggle>
      <span slot="navbar" id="user">
        {state.user ? state.user.username : 'anonymous'}
      </span>
      {state.user ? (
        <Button slot="navbar" id="logout" onClick={async () => await logout()}>
          Log out
        </Button>
      ) : (
        <Button slot="navbar" id="login" onClick={() => navigate('/login')}>
          Log in
        </Button>
      )}

      <Outlet />
    </AppLayout>
  );
}
