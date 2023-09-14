import {createBrowserRouter, RouteObject} from 'react-router-dom';
import {ChatView} from "Frontend/views/ChatView.js";
import {LoginView} from "Frontend/views/LoginView.js";

export const routes: RouteObject[] = [
  { path: '/', element: <ChatView/> },
  { path: '/login', element: <LoginView/> },
];

const router = createBrowserRouter(routes);
export default router;
