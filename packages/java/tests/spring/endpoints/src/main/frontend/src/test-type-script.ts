import { html, LitElement } from 'lit';
import { customElement, query } from 'lit/decorators.js';
import { AnnotatedEndpoint } from 'Frontend/generated/endpoints';
import { Binder } from '@vaadin/hilla-lit-form';
import AnnotatedEntityModel from 'Frontend/generated/com/vaadin/flow/connect/AnnotatedEndpoint/AnnotatedEntityModel';

@customElement('test-type-script')
export class TestTypeScriptComponent extends LitElement {
  @query('#content')
  private content!: HTMLOutputElement;

  render() {
    return html`
      <button id="getAnnotatedEntity" @click="${this.getAnnotatedEntity}">Get annotated entity</button>
      <button id="checkAnnotatedEntityModelType" @click="${this.checkAnnotatedEntityModelType}">
        Check annotated entity model type
      </button>
      <button id="checkAnnotatedEntityModelValidation" @click="${this.checkAnnotatedEntityModelValidation}">
        Check annotated entity model validation
      </button>
      <output id="content"></output>
    `;
  }

  public async getAnnotatedEntity() {
    const annotatedEntity = await AnnotatedEndpoint.getAnnotatedEntity();
    if (annotatedEntity === undefined) {
      throw new Error('Missing entity object result from endpoint');
    }

    this.content.textContent = annotatedEntity.customName;
  }

  public checkAnnotatedEntityModelType() {
    const binder = new Binder(this, AnnotatedEntityModel);
    binder.read({ customName: 'value' });
    this.content.textContent = typeof binder.for(binder.model.customName).value;
  }

  public async checkAnnotatedEntityModelValidation() {
    const binder = new Binder(this, AnnotatedEntityModel);
    binder.read({ customName: ' ' });
    await binder.validate();
    this.content.textContent = binder.errors[0]?.message || '';
  }
}
