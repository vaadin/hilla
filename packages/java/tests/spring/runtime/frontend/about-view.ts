import { LitElement, html } from 'lit';
import { customElement } from 'lit/decorators.js';

@customElement('about-view')
export class AboutView extends LitElement {
  render() {
    return html` <div>About</div> `;
  }
}
