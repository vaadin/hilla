import { LitElement, html } from 'lit';
import { customElement, query } from 'lit/decorators.js';

@customElement('test-view')
export class TestView extends LitElement {
  @query('#sw-content')
  private swContent!: HTMLElement;

  connectedCallback() {
    super.connectedCallback();
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.addEventListener('message', (event) => {
        if (event.data.message !== 'Hi') {
          return;
        }
        this.swContent.textContent = 'Hey from SW';
      });
    }
  }

  render() {
    return html`
      <div>
        <div id="sw-content"></div>
        <div>Test</div>
      </div>
    `;
  }
}
