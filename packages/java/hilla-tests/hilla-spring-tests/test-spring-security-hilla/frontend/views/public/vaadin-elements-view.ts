import { html } from 'lit';
import { customElement, query, state } from 'lit/decorators';
import { until } from 'lit/directives/until';
import { repeat } from 'lit/directives/repeat';

import '@vaadin/form-layout';
import '@vaadin/checkbox';
import '@vaadin/checkbox-group';
import '@vaadin/combo-box';
import '@vaadin/date-picker';
import '@vaadin/date-time-picker';
import '@vaadin/time-picker';
import '@vaadin/select';
import '@vaadin/list-box';
import '@vaadin/item';
import '@vaadin/radio-group';
import '@vaadin/radio-group/vaadin-radio-group';
import '@vaadin/text-field';
import '@vaadin/integer-field';
import '@vaadin/number-field';
import '@vaadin/email-field';
import '@vaadin/rich-text-editor';
import '@vaadin/login';
import '@vaadin/custom-field';
import '@vaadin/button';
import '@vaadin/notification';

import { field, Binder } from '@vaadin/form';

import { View } from '../view';
import ElementsModel from 'Frontend/generated/com/vaadin/flow/spring/fusionsecurity/fusionform/ElementsModel';
import { ElementsEndpoint } from 'Frontend/generated/endpoints';

@customElement('vaadin-elements-view')
export class VaadinElementsView extends View {
  private binder = new Binder(this, ElementsModel);

  private options = ElementsEndpoint.getOptions();
  @query('vaadin-notification') private notification: any;

  @state()
  private loading:boolean = false;

  firstUpdated(arg: any) {
    super.firstUpdated(arg);
    this.binder.read(ElementsModel.createEmptyValue());
  }

  render() {
    return html`
      <vaadin-button @click="${() => this.submit()}" id="save" ?disabled="${this.loading}">save</vaadin-button>
      <vaadin-button @click="${() => this.binder.clear()}">clear</vaadin-button>
      <vaadin-button @click="${() => this.binder.reset()}">reset</vaadin-button>
      <vaadin-button @click="${() => this.loadDataFromEndpoint()}" id="load-from-endpoint">Load Data From Endpoint</vaadin-button>
      <vaadin-form-layout @click="${() => this.notification.close()}">
        <vaadin-checkbox ...="${field(this.binder.model.checkbox)}">checkbox</vaadin-checkbox>
        <vaadin-radio-button ...="${field(this.binder.model.radioButton)}">radio-button</vaadin-radio-button>
        <vaadin-radio-group ...="${field(this.binder.model.radioButtonGroup)}" label="radio-group" invalid>
          ${until(this.options.then(opts => opts && repeat(opts, (item, _i) => html`
            <vaadin-radio-button value="${item}">${item}</vaadin-radio-button>
          `)))}
        </vaadin-radio-group>
        <vaadin-combo-box ...="${field(this.binder.model.comboBox)}" label="combo-box"
          .items="${until(this.options)}">
        </vaadin-combo-box>
        <vaadin-select ...="${field(this.binder.model.select)}" allow-custom-value label="select">
          <template>
            <vaadin-list-box>
              <vaadin-item><span>item-1</span></vaadin-item>
              <vaadin-item><span>item-2</span></vaadin-item>
            </vaadin-list-box>
          </template>
        </vaadin-select>
        <vaadin-custom-field ...="${field(this.binder.model.customField)}" label="custom-field">
          <vaadin-text-field></vaadin-text-field>
        </vaadin-custom-field>
        <vaadin-text-field ...="${field(this.binder.model.textField)}"  label="text-field"></vaadin-text-field>
        <vaadin-password-field ...="${field(this.binder.model.passwordField)}" label="password-field"></vaadin-password-field>
        <vaadin-integer-field ...="${field(this.binder.model.integerField)}" label="integer-field" has-controls></vaadin-integer-field>
        <vaadin-number-field ...="${field(this.binder.model.numberField)}" label="number-field" id="number-field"></vaadin-number-field>
        <vaadin-email-field ...="${field(this.binder.model.emailField)}" label="email-field"></vaadin-email-field>
        <vaadin-text-area ...="${field(this.binder.model.textArea)}" label="textarea"></vaadin-text-area>
        <vaadin-custom-field label="list-box">
          <vaadin-list-box ...="${field(this.binder.model.listBox)}" label="list-box" id="list-box">
            ${until(this.options.then(opts => opts && repeat(opts, (item, _i) => html`
              <vaadin-item><span>${item}</span></vaadin-item>
            `)))}
          </vaadin-list-box>
        </vaadin-custom-field>
        <vaadin-date-picker ...="${field(this.binder.model.datePicker)}" label="date-picker"></vaadin-date-picker>
        <vaadin-date-time-picker ...="${field(this.binder.model.dateTimePicker)}" label="date-time-picker">
          <vaadin-date-time-picker-date-picker slot="date-picker"></vaadin-date-time-picker-date-picker>
          <vaadin-date-time-picker-time-picker slot="time-picker"></vaadin-date-time-picker-time-picker>
        </vaadin-date-time-picker>
        <vaadin-custom-field label="rich-text-editor" colspan="2">
          <vaadin-rich-text-editor ...="${field(this.binder.model.richText)}" label="rich-text-editor" theme="compact"></vaadin-rich-text-editor>
        </vaadin-custom-field>
      </vaadin-form-layout>
      <vaadin-notification duration="0" position="top-stretch" id="notification"></vaadin-notification>
    `;
  }

  async submit() {
    let message: string;
    try {
      await this.binder.submitTo(ElementsEndpoint.saveElements);
      message = "<h3>saved</h3>";
    } catch (error:any) {
      message = error.message.replace(/\n/g, '<br/>');
    }
    this.notification.renderer = (root: Element) => root.innerHTML = '<br/>' + message + '<br/><br/>';
    this.notification.open();
  }

  async loadDataFromEndpoint() {
    this.loading = true;
    const data = await ElementsEndpoint.getElements();
    this.binder.read(data!);
    this.loading = false;
  }
}
