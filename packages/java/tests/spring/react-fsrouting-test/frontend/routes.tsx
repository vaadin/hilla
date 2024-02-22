import { toReactRouter } from '@vaadin/hilla-file-router/runtime.js';
import views from 'Frontend/generated/views.js';
import { serverSideRoutes } from "Frontend/generated/flow/Flow";
import {createBrowserRouter} from "react-router-dom";

const route = toReactRouter(views);
route.children?.push(...serverSideRoutes);

export const routes = [route]
export const router = createBrowserRouter(routes, {basename: new URL(document.baseURI).pathname });
