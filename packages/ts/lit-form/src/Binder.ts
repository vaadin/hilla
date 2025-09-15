import type { Model } from '@vaadin/hilla-models';
import type { LitElement } from 'lit';
import { type BinderConfiguration, BinderRoot } from './BinderRoot.js';
import type { AbstractModel, DetachedModelConstructor, Value } from './Models.js';
import type { ProvisionalModel } from './ProvisionalModel.js';

/**
 * A Binder controls all aspects of a single form.
 * Typically, it is used to get and set the form value,
 * access the form model, validate, reset, and submit the form.
 *
 * @typeParam T - Type of the value that binds to a form
 * @typeParam M - Type of the model that describes the structure of the value
 */
export class Binder<M extends ProvisionalModel> extends BinderRoot<M> {
  declare readonly ['constructor']: Omit<typeof Binder<M>, 'constructor'>;

  context: Element;

  /**
   *
   * @param context - The form view component instance to update.
   * @param modelClass - The constructor (the class reference) of the form model. The Binder instantiates the top-level model
   * @param config - The options object, which can be used to config the onChange and onSubmit callbacks.
   *
   * ```
   * binder = new Binder(orderView, OrderModel);
   * or
   * binder = new Binder(orderView, OrderModel, {onSubmit: async (order) => {endpoint.save(order)}});
   * ```
   */
  constructor(
    context: Element,
    modelClass: DetachedModelConstructor<M & AbstractModel> | (M & Model),
    config?: BinderConfiguration<Value<M>>,
  ) {
    const changeCallback =
      config?.onChange ??
      (typeof (context as LitElement).requestUpdate === 'function'
        ? () => (context as LitElement).requestUpdate()
        : undefined);

    super(modelClass, {
      ...(config ?? {}),
      onChange: changeCallback,
      context,
    });
    this.context = context;
  }
}
