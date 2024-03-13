import router from 'Frontend/routes.js';
import { RouterProvider } from 'react-router-dom';
import { i18n } from "@vaadin/hilla-react-i18n";

await i18n.configure();

export default function App() {
  return <RouterProvider router={router} />;
}
