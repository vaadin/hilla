import { createBrowserRouter } from 'react-router-dom';
import MainLayout from './MainLayout.js';
import BasicSignalView from './views/BasicSignalView.js';
export const routes = [
  {
    path: '',
    element: <MainLayout />,
    children: [
      {
        path: '/basic-signal',
        element: <BasicSignalView />,
      },
    ],
  },
];

const router = createBrowserRouter([...routes]);
export default router;
