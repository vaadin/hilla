import { LitElement, html } from 'lit';
import { customElement } from 'lit/decorators.js';

@customElement('test-view')
export class TestView extends LitElement {
  render() {
    return html` <div>Hello</div> `;
  }
}
