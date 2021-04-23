import { MobxLitElement } from '@adobe/lit-mobx';
import { applyTheme } from 'Frontend/generated/theme';
import { autorun, IAutorunOptions, IReactionDisposer, IReactionOptions, IReactionPublic, reaction } from 'mobx';

export class MobxElement extends MobxLitElement {
  private disposers: IReactionDisposer[] = [];
  /**
   * Creates a MobX reaction using the given parameters and disposes it when this element is detached.
   *
   * This should be called from `connectedCallback` to ensure that the reaction is active also if the element is attached again later.
   */
  protected reaction<T>(
    expression: (r: IReactionPublic) => T,
    effect: (arg: T, prev: T, r: IReactionPublic) => void,
    opts?: IReactionOptions
  ): void {
    this.disposers.push(reaction(expression, effect, opts));
  }

  /**
   * Creates a MobX autorun using the given parameters and disposes it when this element is detached.
   *
   * This should be called from `connectedCallback` to ensure that the reaction is active also if the element is attached again later.
   */
  protected autorun(view: (r: IReactionPublic) => any, opts?: IAutorunOptions): void {
    this.disposers.push(autorun(view, opts));
  }
  disconnectedCallback() {
    super.disconnectedCallback();
    this.disposers.forEach((disposer) => {
      disposer();
    });
    this.disposers = [];
  }
}

export class View extends MobxElement {
  createRenderRoot() {
    // Do not use a shadow root
    return this;
  }
}

export class Layout extends MobxElement {
  connectedCallback() {
    super.connectedCallback();
    applyTheme(this.shadowRoot!);
  }
}
