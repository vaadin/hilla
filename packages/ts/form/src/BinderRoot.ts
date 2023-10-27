import { EndpointValidationError, type ValidationErrorData } from '@hilla/frontend/EndpointErrors.js';
import {
  _clearValidation,
  _setErrorsWithDescendants,
  _update,
  _updateValidation,
  BinderNode,
  CHANGED,
} from './BinderNode.js';
import { type FieldElement, type FieldStrategy, getDefaultFieldStrategy } from './Field.js';
import {
  createDetachedModel,
  _parent,
  type AbstractModel,
  type DetachedModelConstructor,
  type Value,
} from './Models.js';
import {
  type InterpolateMessageCallback,
  runValidator,
  ServerValidator,
  ValidationError,
  type Validator,
  type ValueError,
} from './Validation.js';

export type BinderConfiguration<T> = Readonly<{
  onChange?(oldValue?: T): void;
  onSubmit?(value: T): Promise<T | undefined | void>;
}>;

export type BinderRootConfiguration<T> = BinderConfiguration<T> &
  Readonly<{
    context?: unknown;
  }>;

/**
 * A simplified Binder that does not require a context.
 * It can be used as root when there is no Element to use as context.
 *
 * @typeParam T - Type of the value that binds to a form
 * @typeParam M - Type of the model that describes the structure of the value
 */
export class BinderRoot<M extends AbstractModel = AbstractModel> extends BinderNode<M> {
  static interpolateMessageCallback?: InterpolateMessageCallback<any>;

  #defaultValue!: Value<M>; // Initialized in the `read()` method

  #value!: Value<M>; // Initialized in the `read()` method

  readonly #emptyValue: Value<M>;

  #submitting = false;

  #validating = false;

  #validationRequest?: Promise<void>;

  readonly #config?: BinderRootConfiguration<Value<M>>;

  readonly #validations = new Map<AbstractModel, Map<Validator, Promise<readonly ValueError[]>>>();

  readonly #context: unknown = this;

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
  constructor(Model: DetachedModelConstructor<M>, config?: BinderRootConfiguration<Value<M>>) {
    super(createDetachedModel(Model));
    // @ts-expect-error the model's parent is the binder
    this.model[_parent] = this;
    this.#context = config?.context ?? this;
    this.#config = config;
    // Initialize value instead of the parent.
    this.initializeValue(true);
    this.#emptyValue = this.value;
  }

  /**
   * The initial value of the form, before any fields are edited by the user.
   */
  override get defaultValue(): Value<M> {
    return this.#defaultValue;
  }

  override set defaultValue(newValue: Value<M>) {
    this.#defaultValue = newValue;
    this.dispatchEvent(CHANGED);
  }

  override get binder(): BinderRoot {
    return this as BinderRoot;
  }

  /**
   * The current value of the form.
   */
  override get value(): Value<M> {
    return this.#value;
  }

  override set value(newValue: Value<M>) {
    if (newValue === this.#value) {
      return;
    }

    const oldValue = this.#value;
    this.#value = newValue;
    this[_update](oldValue);
    this[_updateValidation]().catch(() => {});
  }

  /**
   * Indicates the submitting status of the form.
   * True if the form was submitted, but the submit promise is not resolved yet.
   */
  get submitting(): boolean {
    return this.#submitting;
  }

  /**
   * Indicates the validating status of the form.
   * True when there is an ongoing validation.
   */
  get validating(): boolean {
    return this.#validating;
  }

  /**
   * Read the given value into the form and clear validation errors. Clears the form if the value is undefined.
   *
   * @param value - The value to read, or undefined to clear.
   */
  read(value: Value<M> | null | undefined): void {
    if (value === undefined || value === null) {
      this.clear();
      return;
    }
    this.defaultValue = value;
    if (
      // Skip when no value is set yet (e.g., invoked from constructor)
      this.value &&
      // Clear validation state, then proceed if update is needed
      this[_clearValidation]() &&
      // When value is dirty, another update is coming from invoking the value
      // setter below, so we skip this one to prevent duplicate updates
      this.value === value
    ) {
      this[_update](this.value);
    }

    this.value = this.defaultValue;
  }

  /**
   * Reset the form to the previous value
   */
  reset(): void {
    this.read(this.#defaultValue);
  }

  /**
   * Sets the form to empty value, as defined in the Model.
   */
  clear(): void {
    this.read(this.#emptyValue);
  }

  /**
   * Submit the current form value to a predefined
   * onSubmit callback.
   *
   * It's a no-op if the onSubmit callback is undefined.
   */
  async submit(): Promise<Value<M> | undefined | void> {
    const onSubmit = this.#config?.onSubmit;
    if (onSubmit) {
      return this.submitTo(onSubmit);
    }

    return undefined;
  }

  /**
   * Submit the current form value to callback
   *
   * @param endpointMethod - the callback function
   */
  async submitTo<V>(endpointMethod: (value: Value<M>) => Promise<V>): Promise<V> {
    const errors = await this.validate();
    if (errors.length) {
      throw new ValidationError(errors);
    }

    this.#submitting = true;
    this[_update](this.value);
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
        this[_setErrorsWithDescendants](valueErrors);
        throw new ValidationError(valueErrors);
      }

      throw error;
    } finally {
      this.#submitting = false;
      this.defaultValue = this.value;
      this[_update](this.value);
    }
  }

  async requestValidation<NM extends AbstractModel>(
    model: NM,
    validator: Validator<Value<NM>>,
  ): Promise<ReadonlyArray<ValueError<Value<NM>>>> {
    let modelValidations: Map<Validator<Value<NM>>, Promise<ReadonlyArray<ValueError<Value<NM>>>>>;

    if (this.#validations.has(model)) {
      modelValidations = this.#validations.get(model) as Map<
        Validator<Value<NM>>,
        Promise<ReadonlyArray<ValueError<Value<NM>>>>
      >;
    } else {
      modelValidations = new Map();
      this.#validations.set(model, modelValidations);
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
      this.#validations.delete(model);
    }
    if (this.#validations.size === 0) {
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
  getFieldStrategy<TField>(elm: HTMLElement, model?: AbstractModel<TField>): FieldStrategy {
    return getDefaultFieldStrategy(elm as FieldElement, model);
  }

  protected performValidation(): Promise<void> | void {
    if (!this.#validationRequest) {
      this.#validating = true;
      this.dispatchEvent(CHANGED);
      this.#validationRequest = Promise.resolve().then(() => {
        this.#validationRequest = undefined;
      });
    }
    return this.#validationRequest;
  }

  protected completeValidation(): void {
    this.#validating = false;
    this.dispatchEvent(CHANGED);
  }

  protected override [_update](oldValue: Value<M>): void {
    this.#config?.onChange?.call(this.#context, oldValue);
    this.dispatchEvent(CHANGED);
  }
}
