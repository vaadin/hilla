import { html, LitElement } from 'lit';
import { customElement, state } from 'lit/decorators.js';
import { AppEndpoint } from "./generated/endpoints";


@customElement("test-component")
export class TestComponent extends LitElement {

  @state()
  response: string = "";

  protected createRenderRoot(): Element | ShadowRoot {
    return this;
  }
  render() {
    return html`
    <input type="text" id="name" />
        <button id="button" @click="${() => this.callendpoint()}">call endpoint</button>
        <div id="response">
          ${this.response}
        </div>
    `;
  }

  async callendpoint() {
    const name = (this.renderRoot.querySelector("#name")! as any).value;
    this.response = await AppEndpoint.hello(name);
  }
}
