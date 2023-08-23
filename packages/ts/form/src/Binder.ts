import type { LitElement } from 'lit';
import { BinderRoot, type BinderConfiguration } from './BinderRoot.js';
import type { AbstractModel, ModelConstructor } from './Models.js';

/**
 * A Binder controls all aspects of a single form.
 * Typically, it is used to get and set the form value,
 * access the form model, validate, reset, and submit the form.
 *
 * @typeParam T - Type of the value that binds to a form
 * @typeParam M - Type of the model that describes the structure of the value
 */
export class Binder<T, M extends AbstractModel<T>> extends BinderRoot<T, M> {
  context: Element;

  /**
   *
   * @param context - The form view component instance to update.
   * @param Model - The constructor (the class reference) of the form model. The Binder instantiates the top-level model
   * @param config - The options object, which can be used to config the onChange and onSubmit callbacks.
   *
   * ```
   * binder = new Binder(orderView, OrderModel);
   * or
   * binder = new Binder(orderView, OrderModel, {onSubmit: async (order) => {endpoint.save(order)}});
   * ```
   */
  constructor(context: Element, Model: ModelConstructor<T, M>, config?: BinderConfiguration<T>) {
    super(Model, config);
    this.context = context;

    if (typeof (context as LitElement).requestUpdate === 'function') {
      this.changeCallback = () => (context as LitElement).requestUpdate();
    }

    if (config?.onChange) {
      this.changeCallback = config.onChange.bind(this);
    }

    this.readValue();
  }

  protected override getCallbackContext(): Element {
    return this.context;
  }
}
