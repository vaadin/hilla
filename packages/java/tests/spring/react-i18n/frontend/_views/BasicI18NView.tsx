import { i18n, translate } from '@vaadin/hilla-react-i18n';
import { TextField } from '@vaadin/react-components';

export default function BasicI18NView() {
  return (
    <>
      <section className="flex p-m gap-m items-end">
        <TextField id='name' label={translate(i18n`basic.form.name.label`)}/>
        <TextField id='address' label={translate(i18n`basic.form.address.label`)}/>
        <TextField
          id='language'
          label='Language'
          onValueChanged={async (e) => {
            await i18n.setLanguage(e.detail.value);
          }}
        />
        <span id="out">
          Language: {i18n.language.value}
          <br/>
          Resolved Language: {i18n.resolvedLanguage.value}
        </span>
      </section>
    </>
  );
}
