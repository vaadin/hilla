import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { i18n, key, translate } from '@vaadin/hilla-react-i18n';
import { TextField } from '@vaadin/react-components';
import type { Detail } from '../types/detail.js';

export const config: ViewConfig<Detail> = {
  title: key`viewtitle.basic`,
  lazy: false,
  detail: {
    description: key`viewdescription.basic`,
  },
};

export default function BasicI18NView() {
  return (
    <section className="flex p-m gap-m items-end">
      <TextField id="name" label={translate(key`basic.form.name.label`)} />
      <TextField id="address" label={translate(key`basic.form.address.label`)} />
      <TextField id="language" label="Language" onValueChanged={(e) => i18n.setLanguage(e.detail.value)} />
      <span id="out">
        Language: {i18n.language.value}
        <br />
        Resolved Language: {i18n.resolvedLanguage.value}
      </span>
    </section>
  );
}
