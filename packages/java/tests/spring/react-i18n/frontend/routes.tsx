import { createBrowserRouter } from 'react-router-dom';
import MainLayout from './MainLayout.js';
import BasicI18NView from './_views/BasicI18NView';
export const routes = [
  {
    path: '',
    element: <MainLayout />,
    children: [
      {
        path: '/basic-i18n',
        element: <BasicI18NView />,
      },
    ],
  },
];

const router = createBrowserRouter([...routes], {
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
export default router;
