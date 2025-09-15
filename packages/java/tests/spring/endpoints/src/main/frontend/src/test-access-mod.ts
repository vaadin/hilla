import { html, LitElement } from 'lit';
import { customElement, query } from 'lit/decorators.js';
import { AccessModifierEndpoint } from 'Frontend/generated/endpoints';

@customElement('test-access-mod')
export class TestAccessModifierComponent extends LitElement {
  @query('#content')
  private content!: HTMLOutputElement;
  @query('#methods')
  private methods!: HTMLOutputElement;

  render() {
    return html`
      <button id="getEntity" @click="${this.getEntity}">Get entity</button>
      <output id="content"></output>
      <object id="methods"></object>
    `;
  }

  public async getEntity() {
    const entity = await AccessModifierEndpoint.getEntity();
    if (entity === undefined) {
      throw new Error('Missing entity object result from endpoint');
    }

    this.content.textContent = JSON.stringify(entity);
    this.methods.textContent = Object.keys(entity).join(', ');
  }
}
