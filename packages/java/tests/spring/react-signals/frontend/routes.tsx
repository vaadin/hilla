import { createBrowserRouter } from 'react-router-dom';
import MainLayout from './MainLayout.js';
import BasicSignalView from './_views/BasicSignalView.js';
import SharedNumberSignal from './_views/SharedNumberSignal.js';
export const routes = [
  {
    path: '',
    element: <MainLayout />,
    children: [
      {
        path: '/basic-signal',
        element: <BasicSignalView />,
      },
      {
        path: '/number-signal',
        element: <SharedNumberSignal />,
      },
    ],
  },
];

const router = createBrowserRouter([...routes]);
export default router;
