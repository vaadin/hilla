import router from 'Frontend/routes.js';
import { AuthContext, useAuth } from 'Frontend/useAuth.js';
import { RouterProvider } from 'react-router';

export default function App() {
  const auth = useAuth();
  return (
    <AuthContext.Provider value={auth}>
      <RouterProvider router={router} />
    </AuthContext.Provider>
  );
}
