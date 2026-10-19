import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { i18n, key, translate } from '@vaadin/hilla-react-i18n';
import { TextField } from '@vaadin/react-components';

export const config: ViewConfig = {
  title: 'I18n',
};

export default function I18nView(): React.JSX.Element {
  return (
    <section className="flex p-m gap-m items-end">
      <span id="greeting">{translate(key`greeting`)}</span>
      <TextField id="language" label="Language" onValueChanged={(e) => i18n.setLanguage(e.detail.value)} />
      <span id="resolved-language">{i18n.resolvedLanguage.value}</span>
    </section>
  );
}
