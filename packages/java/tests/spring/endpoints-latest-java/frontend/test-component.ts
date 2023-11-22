import { html, LitElement, type TemplateResult } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import { AppEndpoint } from './generated/endpoints';

@customElement('test-component')
export class TestComponent extends LitElement {
  @state()
  response: string = '';

  async callendpoint(): Promise<void> {
    const name = this.renderRoot.querySelector<HTMLInputElement>('#name')!.value;
    this.response = await AppEndpoint.hello(name);
  }

  render(): TemplateResult {
    return html`
      <input type="text" id="name" />
      <button id="button" @click="${async () => this.callendpoint()}">call endpoint</button>
      <div id="response">${this.response}</div>
    `;
  }

  protected createRenderRoot(): HTMLElement | DocumentFragment {
    return this;
  }
}
