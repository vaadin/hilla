import {createBrowserRouter, RouteObject} from 'react-router-dom';
import {ChatView} from "Frontend/views/ChatView.js";
import {LoginView} from "Frontend/views/LoginView.js";
import {ReadOnlyGrid} from "Frontend/views/ReadOnlyGrid";

export const routes: RouteObject[] = [
      { path: '/', element: <ChatView />, handle: { title: 'Chat', requiresLogin: true } },
      { path: '/grid', element: <ReadOnlyGrid/>, handle: { title: 'Grid' } },
      { path: '/login', element: <LoginView /> }
];

export default createBrowserRouter(routes, {
  future: {
    // eslint-disable-next-line camelcase
    v7_fetcherPersist: true,
    // eslint-disable-next-line camelcase
    v7_normalizeFormMethod: true,
    // eslint-disable-next-line camelcase
    v7_partialHydration: true,
    // eslint-disable-next-line camelcase
    v7_relativeSplatPath: true,
    // eslint-disable-next-line camelcase
    v7_skipActionErrorRevalidation: true,
  },
});
