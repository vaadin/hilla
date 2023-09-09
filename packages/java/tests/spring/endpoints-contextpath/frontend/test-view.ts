import { LitElement, html } from 'lit';
import { customElement, query } from 'lit/decorators.js';
import { EchoEndpoint } from './generated/endpoints';

@customElement('test-view')
export class TestView extends LitElement {
  @query('#input')
  input!: HTMLInputElement;
  @query('#result')
  result!: HTMLInputElement;

  render() {
    return html`
      <div style="display: inline-flex;flex-direction:column">
        <input type="text" id="input" value="hello" />
        <div>Result: <span id="result"></span></div>
        <button
          id="normalEndpoint"
          @click=${() => {
            this.result.innerText = '';
            EchoEndpoint.echo(this.input.value).then((resp) => (this.result.innerText += resp));
          }}
        >
          Echo using normal endpoint
        </button>
        <button
          id="fluxEndpoint"
          @click=${() => {
            this.result.innerText = '';
            EchoEndpoint.fluxEcho(this.input.value).onNext((resp) => (this.result.innerText += resp));
          }}
        >
          Echo using reactive endpoint
        </button>
      </div>
    `;
  }
}
