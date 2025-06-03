import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { key, translate } from '@vaadin/hilla-react-i18n';
import type { Detail } from './detail';

export const config: ViewConfig<Detail> = {
  title: key`viewtitle.home`,
  lazy: false,
  detail: {
    description: key`viewdescription.home`,
  },
};

export default function HomeView() {
  return <section className="flex p-m gap-m items-end">{translate(key`home.intro`)}</section>;
}
