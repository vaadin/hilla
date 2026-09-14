import { createElement } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router';
import { router } from 'Frontend/generated/routes.js';
import { AuthProvider } from 'Frontend/util/auth.js';

function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  );
}

createRoot(document.getElementById('outlet')!).render(createElement(App));
