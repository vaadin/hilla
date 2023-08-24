import { EndpointValidationError, type ValidationErrorData } from '@hilla/frontend/EndpointErrors.js';
import { BinderNode, CHANGED } from './BinderNode.js';
import { type FieldStrategy, getDefaultFieldStrategy } from './Field.js';
import { _parent, type AbstractModel, type HasValue, type ModelConstructor } from './Models.js';
import {
  type InterpolateMessageCallback,
  runValidator,
  ServerValidator,
  ValidationError,
  type Validator,
  type ValueError,
} from './Validation.js';

export { CHANGED };

const _submitting = Symbol('submitting');
const _defaultValue = Symbol('defaultValue');
const _value = Symbol('value');
const _emptyValue = Symbol('emptyValue');
const _onChange = Symbol('onChange');
const _onSubmit = Symbol('onSubmit');
const _validations = Symbol('validations');
const _validating = Symbol('validating');
const _validationRequestSymbol = Symbol('validationRequest');

export type BinderConfiguration<T> = Readonly<{
  onChange?(oldValue?: T): void;
  onSubmit?(value: T): Promise<T | undefined> | Promise<void>;
}>;

export type BinderRootConfiguration<T> = BinderConfiguration<T> &
  Readonly<{
    context?: any;
  }>;

/**
 * A simplified Binder that does not require a context.
 * It can be used as root when there is no Element to use as context.
 *
 * @typeParam T - Type of the value that binds to a form
 * @typeParam M - Type of the model that describes the structure of the value
 */
export class BinderRoot<T, M extends AbstractModel<T>> extends BinderNode<T, M> {
  static interpolateMessageCallback?: InterpolateMessageCallback<any>;

  private [_defaultValue]!: T; // Initialized in the `read()` method

  private [_value]!: T; // Initialized in the `read()` method

  private [_emptyValue]: T;

  private [_submitting] = false;

  private [_validating] = false;

  private [_validationRequestSymbol]?: Promise<void>;

  private [_onChange]?: (oldValue?: T) => void;

  private [_onSubmit]?: (value: T) => Promise<any>;

  private [_validations] = new Map<AbstractModel<any>, Map<Validator<any>, Promise<ReadonlyArray<ValueError<any>>>>>();

  #context: any = this;

  /**
   *
   * @param Model - The constructor (the class reference) of the form model. The Binder instantiates the top-level model
   * @param config - The options object, which can be used to config the onChange and onSubmit callbacks.
   *
   * ```
   * binder = new BinderRoot(OrderModel);
   * or
   * binder = new BinderRoot(OrderModel, {onSubmit: async (order) => {endpoint.save(order)}});
   * ```
   */
  constructor(Model: ModelConstructor<T, M>, config?: BinderRootConfiguration<T>) {
    const valueContainer: HasValue<T> = { value: undefined };
    super(new Model(valueContainer, 'value', false));
    this[_emptyValue] = valueContainer.value!;
    // @ts-expect-error the model's parent is the binder
    this.model[_parent] = this;
    this.#context = config?.context ?? this;
    this[_onChange] = config?.onChange ?? (() => {});
    this.read(this[_emptyValue]);
    this[_onSubmit] = config?.onSubmit ?? this[_onSubmit];
  }

  /**
   * The initial value of the form, before any fields are edited by the user.
   */
  override get defaultValue(): T {
    return this[_defaultValue];
  }

  override set defaultValue(newValue: T) {
    this[_defaultValue] = newValue;
    this.dispatchEvent(CHANGED);
  }

  /**
   * The current value of the form.
   */
  override get value(): T {
    return this[_value];
  }

  override set value(newValue: T) {
    if (newValue === this[_value]) {
      return;
    }

    const oldValue = this[_value];
    this[_value] = newValue;
    this.update(oldValue);
    this.updateValidation()
      .then(() => this.dispatchEvent(CHANGED))
      .catch(() => {});
    this.dispatchEvent(CHANGED);
  }

  /**
   * Indicates the submitting status of the form.
   * True if the form was submitted, but the submit promise is not resolved yet.
   */
  get submitting(): boolean {
    return this[_submitting];
  }

  /**
   * Indicates the validating status of the form.
   * True when there is an ongoing validation.
   */
  get validating(): boolean {
    return this[_validating];
  }

  /**
   * Read the given value into the form and clear validation errors
   *
   * @param value - Sets the argument as the new default
   * value before resetting, otherwise the previous default is used.
   */
  read(value: T): void {
    this.defaultValue = value;
    if (
      // Skip when no value is set yet (e.g., invoked from constructor)
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
  reset(): void {
    this.read(this[_defaultValue]);
  }

  /**
   * Sets the form to empty value, as defined in the Model.
   */
  clear(): void {
    this.read(this[_emptyValue]);
  }

  /**
   * Submit the current form value to a predefined
   * onSubmit callback.
   *
   * It's a no-op if the onSubmit callback is undefined.
   */
  async submit(): Promise<T | undefined> {
    if (this[_onSubmit] !== undefined) {
      return this.submitTo(this[_onSubmit]!);
    }
    return undefined;
  }

  /**
   * Submit the current form value to callback
   *
   * @param endpointMethod - the callback function
   */
  async submitTo<V>(endpointMethod: (value: T) => Promise<V>): Promise<V> {
    const errors = await this.validate();
    if (errors.length) {
      throw new ValidationError(errors);
    }

    this[_submitting] = true;
    this.update(this.value);
    this.dispatchEvent(CHANGED);
    try {
      return await endpointMethod.call(this.#context, this.value);
    } catch (error: unknown) {
      if (error instanceof EndpointValidationError && error.validationErrorData.length) {
        const valueErrors: Array<ValueError<any>> = [];
        error.validationErrorData.forEach((data: ValidationErrorData) => {
          const res =
            /Object of type '(.+)' has invalid property '(.+)' with value '(.+)', validation error: '(.+)'/u.exec(
              data.message,
            );
          const [property, value, message] = res ? res.splice(2) : [data.parameterName ?? '', undefined, data.message];
          valueErrors.push({
            message,
            property,
            validator: new ServerValidator(message),
            value,
          });
        });
        this.setErrorsWithDescendants(valueErrors);
        throw new ValidationError(valueErrors);
      }

      throw error;
    } finally {
      this[_submitting] = false;
      this.defaultValue = this.value;
      this.update(this.value);
      this.dispatchEvent(CHANGED);
    }
  }

  async requestValidation<NT, NM extends AbstractModel<NT>>(
    model: NM,
    validator: Validator<NT>,
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
      return modelValidations.get(validator)!;
    }

    const promise = runValidator(model, validator, BinderRoot.interpolateMessageCallback);
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
   * @param elm - the bound element
   * @param model - the bound model
   */
  getFieldStrategy<TField>(elm: any, model?: AbstractModel<TField>): FieldStrategy {
    return getDefaultFieldStrategy(elm, model);
  }

  protected performValidation(): Promise<void> | void {
    if (!this[_validationRequestSymbol]) {
      this[_validating] = true;
      this.dispatchEvent(CHANGED);
      this[_validationRequestSymbol] = Promise.resolve().then(() => {
        this[_validationRequestSymbol] = undefined;
      });
    }
    return this[_validationRequestSymbol];
  }

  protected completeValidation(): void {
    this[_validating] = false;
    this.dispatchEvent(CHANGED);
  }

  protected override update(oldValue: T): void {
    this[_onChange]?.call(this.#context, oldValue);
  }
}
