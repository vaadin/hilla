import '@vaadin/button';
import '@vaadin/text-field';
import { GreetingService } from 'Frontend/generated/endpoints';
import { html } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import { View } from '../view';
import { Notification } from '@vaadin/notification';

@customElement('proxied-service-test-view')
export class ProxiedServiceTestView extends View {

  async connectedCallback() {
    super.connectedCallback();
  }

  render() {
    return html`<div style="display:flex;flex-direction:column;height:100%">
      <vaadin-button id="say-hello-btn" @click="${() => this.callSayHello()}">Say Hello!</vaadin-button>
    </div>`;
  }

  public async callSayHello() {
    GreetingService.sayHello().then((response) => Notification.show(response));
  }
}
