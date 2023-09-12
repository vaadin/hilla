import { createRoot } from 'react-dom/client';
import router from 'Frontend/routes.js';
import { RouterProvider } from 'react-router-dom';

const root = createRoot(document.getElementById('outlet')!);
root.render(
  <RouterProvider router={router} />
);
