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

const router = createBrowserRouter([...routes]);
export default router;
