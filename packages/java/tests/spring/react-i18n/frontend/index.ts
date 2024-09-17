import { createElement } from 'react';
import { createRoot } from 'react-dom/client';
import App from './App.js';
import {i18n} from "@vaadin/hilla-react-i18n";

i18n.configure().then(() =>
  createRoot(document.getElementById('outlet')!).render(createElement(App)));
