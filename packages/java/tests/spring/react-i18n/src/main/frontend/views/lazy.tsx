import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { key, translate } from '@vaadin/hilla-react-i18n';
import { Button } from '@vaadin/react-components';
import type { Detail } from '../types/detail.js';

export const config: ViewConfig<Detail> = {
  title: key`viewtitle.lazy`,
  lazy: true,
  detail: {
    description: key`viewdescription.lazy`,
  },
};

export default function LazyView() {
  return (
    <section className="flex p-m gap-m items-end">
      {translate(key`lazy.intro`)}
      <Button>{translate(key`lazy.button.label`)}</Button>
    </section>
  );
}
