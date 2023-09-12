import { createBrowserRouter } from 'react-router-dom';
import MainLayout from './MainLayout';
import { ReadOnlyGrid } from './views/ReadOnlyGrid';

export const routes = [
  {
    path: '',
    element: <MainLayout />,
    children: [
      {
        path: '/readonly-grid',
        element: <ReadOnlyGrid />,
      },
    ],
  },
];

const router = createBrowserRouter([...routes]);
export default router;
