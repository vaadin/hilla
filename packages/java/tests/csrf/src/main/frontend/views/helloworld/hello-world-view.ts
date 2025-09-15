import { html, LitElement } from 'lit';
import { customElement } from 'lit/decorators.js';

@customElement('hello-world-view')
export class HelloWorldView extends LitElement {
  render() {
    return html` <div>HelloWorldView</div> `;
  }
}
