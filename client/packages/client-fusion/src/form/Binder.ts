// TODO: Fix dependency cycle

// eslint-disable-next-line import/no-cycle
import { BinderNode } from './BinderNode';
// eslint-disable-next-line import/no-cycle
import { _parent, AbstractModel, ModelConstructor } from './Models';
// eslint-disable-next-line import/no-cycle
import { runValidator, ServerValidator, ValidationError, Validator, ValueError } from './Validation';
// eslint-disable-next-line import/no-cycle
import { FieldStrategy, getDefaultFieldStrategy } from './Field';

const _submitting = Symbol('submitting');
const _defaultValue = Symbol('defaultValue');
const _value = Symbol('value');
const _emptyValue = Symbol('emptyValue');
const _onChange = Symbol('onChange');
const _onSubmit = Symbol('onSubmit');
const _validations = Symbol('validations');
const _validating = Symbol('validating');
const _validationRequestSymbol = Symbol('validationRequest');

/**
 * A Binder controls all aspects of a single form.
 * Typically it is used to get and set the form value,
 * access the form model, validate, reset, and submit the form.
 *
 * @param <T> is the type of the value that binds to a form
 * @param <M> is the type of the model that describes the structure of the value
 */
export class Binder<T, M extends AbstractModel<T>> extends BinderNode<T, M> {
  private [_defaultValue]: T;

  private [_value]: T;

  private [_emptyValue]: T;

  private [_submitting] = false;

  private [_validating] = false;

  private [_validationRequestSymbol]: Promise<void> | undefined = undefined;

  private [_onChange]: (oldValue?: T) => void;

  private [_onSubmit]: (value: T) => Promise<any>;

  private [_validations]: Map<AbstractModel<any>, Map<Validator<any>, Promise<ReadonlyArray<ValueError<any>>>>> =
    new Map();

  /**
   *
   * @param context The form view component instance to update.
   * @param Model The constructor (the class reference) of the form model. The Binder instantiates the top-level model
   * @param config The options object, which can be used to config the onChange and onSubmit callbacks.
   *
   * ```
   * binder = new Binder(orderView, OrderModel);
   * or
   * binder = new Binder(orderView, OrderModel, {onSubmit: async (order) => {endpoint.save(order)}});
   * ```
   */
  constructor(public context: Element, Model: ModelConstructor<T, M>, config?: BinderConfiguration<T>) {
    super(new Model({ value: undefined }, 'value', false));
    this[_emptyValue] = (this.model[_parent] as { value: T }).value;
    // @ts-ignore
    this.model[_parent] = this;

    if (typeof (context as any).requestUpdate === 'function') {
      this[_onChange] = () => (context as any).requestUpdate();
    }
    this[_onChange] = config?.onChange || this[_onChange];
    this[_onSubmit] = config?.onSubmit || this[_onSubmit];
    this.read(this[_emptyValue]);
  }

  /**
   * The initial value of the form, before any fields are edited by the user.
   */
  get defaultValue() {
    return this[_defaultValue];
  }

  set defaultValue(newValue) {
    this[_defaultValue] = newValue;
  }

  /**
   * The current value of the form.
   */
  get value() {
    return this[_value];
  }

  set value(newValue) {
    if (newValue === this[_value]) {
      return;
    }

    const oldValue = this[_value];
    this[_value] = newValue;
    this.update(oldValue);
    this.updateValidation();
  }

  /**
   * Read the given value into the form and clear validation errors
   *
   * @param value Sets the argument as the new default
   * value before resetting, otherwise the previous default is used.
   */
  read(value: T) {
    this.defaultValue = value;
    if (
      // Skip when no value is set yet (e. g., invoked from constructor)
      this.value &&
      // Clear validation state, then proceed if update is needed
      this.clearValidation() &&
      // When value is dirty, another update is coming from invoking the value
      // setter below, so we skip this one to prevent duplicate updates
      this.value === value
    ) {
      this.update(this.value);
    }

    this.value = this.defaultValue;
  }

  /**
   * Reset the form to the previous value
   */
  reset() {
    this.read(this[_defaultValue]);
  }

  /**
   * Sets the form to empty value, as defined in the Model.
   */
  clear() {
    this.read(this[_emptyValue]);
  }

  /**
   * Submit the current form value to a predefined
   * onSubmit callback.
   *
   * It's a no-op if the onSubmit callback is undefined.
   */
  async submit(): Promise<T | void> {
    if (this[_onSubmit] !== undefined) {
      return this.submitTo(this[_onSubmit]);
    }
    return undefined;
  }

  /**
   * Submit the current form value to callback
   *
   * @param endpointMethod the callback function
   */
  async submitTo<V>(endpointMethod: (value: T) => Promise<V>): Promise<V> {
    const errors = await this.validate();
    if (errors.length) {
      throw new ValidationError(errors);
    }

    this[_submitting] = true;
    this.update(this.value);
    try {
      return await endpointMethod.call(this.context, this.value);
    } catch (error) {
      if (error.validationErrorData && error.validationErrorData.length) {
        const valueErrors: Array<ValueError<any>> = [];
        error.validationErrorData.forEach((data: any) => {
          const res =
            /Object of type '(.+)' has invalid property '(.+)' with value '(.+)', validation error: '(.+)'/.exec(
              data.message
            );
          const [property, value, message] = res ? res.splice(2) : [data.parameterName, undefined, data.message];
          valueErrors.push({ property, value, validator: new ServerValidator(message), message });
        });
        this.setErrorsWithDescendants(valueErrors);
        throw new ValidationError(valueErrors);
      }

      throw error;
    } finally {
      this[_submitting] = false;
      this.defaultValue = this.value;
      this.update(this.value);
    }
  }

  async requestValidation<NT, NM extends AbstractModel<NT>>(
    model: NM,
    validator: Validator<NT>
  ): Promise<ReadonlyArray<ValueError<NT>>> {
    let modelValidations: Map<Validator<NT>, Promise<ReadonlyArray<ValueError<NT>>>>;
    if (this[_validations].has(model)) {
      modelValidations = this[_validations].get(model) as Map<Validator<NT>, Promise<ReadonlyArray<ValueError<NT>>>>;
    } else {
      modelValidations = new Map();
      this[_validations].set(model, modelValidations);
    }

    await this.performValidation();

    if (modelValidations.has(validator)) {
      return modelValidations.get(validator) as Promise<ReadonlyArray<ValueError<NT>>>;
    }

    const promise = runValidator(model, validator);
    modelValidations.set(validator, promise);
    const valueErrors = await promise;

    modelValidations.delete(validator);
    if (modelValidations.size === 0) {
      this[_validations].delete(model);
    }
    if (this[_validations].size === 0) {
      this.completeValidation();
    }

    return valueErrors;
  }

  /**
   * Determines and returns the field directive strategy for the bound element.
   * Override to customise the binding strategy for a component.
   * The Binder extends BinderNode, see the inherited properties and methods below.
   *
   * @param elm the bound element
   */
  getFieldStrategy(elm: any): FieldStrategy {
    return getDefaultFieldStrategy(elm);
  }

  /**
   * Indicates the submitting status of the form.
   * True if the form was submitted, but the submit promise is not resolved yet.
   */
  get submitting() {
    return this[_submitting];
  }

  /**
   * Indicates the validating status of the form.
   * True when there is an ongoing validation.
   */
  get validating() {
    return this[_validating];
  }

  protected performValidation(): Promise<void> | void {
    if (!this[_validationRequestSymbol]) {
      this[_validating] = true;
      this[_validationRequestSymbol] = Promise.resolve().then(() => {
        this[_validationRequestSymbol] = undefined;
      });
    }
    return this[_validationRequestSymbol];
  }

  protected completeValidation() {
    this[_validating] = false;
  }

  protected update(oldValue: T) {
    if (this[_onChange]) {
      this[_onChange].call(this.context, oldValue);
    }
  }
}

export interface BinderConfiguration<T> {
  onChange?: (oldValue?: T) => void;
  onSubmit?: (value: T) => Promise<T | void>;
}
