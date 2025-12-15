import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { key, translate } from '@vaadin/hilla-react-i18n';
import type { Detail } from '../types/detail.js';

export const config: ViewConfig<Detail> = {
  title: key`viewtitle.home`,
  detail: {
    description: key`viewdescription.home`,
  },
};

export default function HomeView() {
  return <section className="flex p-m gap-m items-end">{translate(key`home.intro`)}</section>;
}
