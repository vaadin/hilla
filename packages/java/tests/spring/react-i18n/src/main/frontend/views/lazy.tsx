import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { key, translate } from '@vaadin/hilla-react-i18n';
import { Detail } from './detail';
import { Button } from '@vaadin/react-components';

export const config: ViewConfig<Detail> = {
  title: 'viewtitle.lazy',
  lazy: true,
  detail: {
    description: 'viewdescription.lazy',
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
