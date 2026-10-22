import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';

export const config: ViewConfig = {
  title: 'Home',
};

export default function HomeView(): React.JSX.Element {
  return <span id="home">Hilla compiled to native</span>;
}
