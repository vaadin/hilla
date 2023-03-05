import { LitElement } from 'lit';
import { BinderNode } from './BinderNode.js';
import { _parent, type AbstractModel, type ModelConstructor } from './Models.js';
import {
  type InterpolateMessageCallback,
  runValidator,
  ServerValidator,
  ValidationError,
  type Validator,
  type ValueError,
} from './Validation.js';
import { type FieldStrategy, getDefaultFieldStrategy } from './Field.js';

export type BinderConfiguration<T> = Readonly<{
  onChange?(oldValue?: T): void;
  onSubmit?(value: T): Promise<T | undefined>;
}>;

type EndpointValidationErrorData = Readonly<{
  message: string;
  parameterName: string;
}>;

type EndpointValidationError = Readonly<{
  message: string;
  validationErrorData: readonly EndpointValidationErrorData[];
}>;

function isEndpointValidationError(error: unknown): error is EndpointValidationError {
  return (
    typeof error === 'object' &&
    error != null &&
    'validationErrorData' in error &&
    Array.isArray(error.validationErrorData) &&
    error.validationErrorData.length !== 0
  );
}

const endpointValidationErrorDataPattern =
  /Object of type '(.+)' has invalid property '(.+)' with value '(.+)', validation error: '(.+)'/u;

/**
 * A Binder controls all aspects of a single form.
 * Typically, it is used to get and set the form value,
 * access the form model, validate, reset, and submit the form.
 *
 * @param <T> is the type of the value that binds to a form
 * @param <M> is the type of the model that describes the structure of the value
 */
export class Binder<T, M extends AbstractModel<T>> extends BinderNode<T, M> {
  static interpolateMessageCallback?: InterpolateMessageCallback<any>;
  context: Element;
  #defaultValue!: T; // Initialized in the `read()` method
  readonly #emptyValue: T;
  readonly #onChange?: (oldValue?: T) => void;
  readonly #onSubmit?: (value: T) => Promise<unknown>;
  #submitting = false;
  #validating = false;
  #validationRequestSymbol?: Promise<void>;
  #validations = new Map<
    AbstractModel<unknown>,
    Map<Validator<AbstractModel<unknown>>, Promise<ReadonlyArray<ValueError<unknown>>>>
  >();

  #value!: T; // Initialized in the `read()` method

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
  constructor(context: Element, Model: ModelConstructor<T, M>, config?: BinderConfiguration<T>) {
    super(new Model({ value: undefined }, 'value', false));
    this.context = context;
    this.#emptyValue = (this.model[_parent] as { value: T }).value;
    this.model[_parent] = this;

    if (context instanceof LitElement) {
      this.#onChange = () => context.requestUpdate();
    }
    this.#onChange = config?.onChange ?? this.#onChange;
    this.#onSubmit = config?.onSubmit ?? this.#onSubmit;
    this.read(this.#emptyValue);
  }

  /**
   * The initial value of the form, before any fields are edited by the user.
   */
  override get defaultValue(): T {
    return this.#defaultValue;
  }

  override set defaultValue(newValue: T) {
    this.#defaultValue = newValue;
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
   * The current value of the form.
   */
  override get value(): T {
    return this.#value;
  }

  override set value(newValue: T) {
    if (newValue === this.#value) {
      return;
    }

    const oldValue = this.#value;
    this.#value = newValue;
    this.update(oldValue);
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.updateValidation();
  }

  /**
   * Sets the form to empty value, as defined in the Model.
   */
  clear(): void {
    this.read(this.#emptyValue);
  }

  /**
   * Determines and returns the field directive strategy for the bound element.
   * Override to customise the binding strategy for a component.
   * The Binder extends BinderNode, see the inherited properties and methods below.
   *
   * @param elm the bound element
   * @param model the bound model
   */
  // eslint-disable-next-line class-methods-use-this
  getFieldStrategy<A>(elm: any, model?: AbstractModel<A>): FieldStrategy {
    return getDefaultFieldStrategy(elm, model);
  }

  /**
   * Read the given value into the form and clear validation errors
   *
   * @param value Sets the argument as the new default
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

  async requestValidation(
    model: AbstractModel<unknown>,
    validator: Validator<AbstractModel<unknown>>,
  ): Promise<ReadonlyArray<ValueError<unknown>>> {
    let modelValidations: Map<Validator<AbstractModel<unknown>>, Promise<ReadonlyArray<ValueError<unknown>>>>;
    if (this.#validations.has(model)) {
      modelValidations = this.#validations.get(model)!;
    } else {
      modelValidations = new Map();
      this.#validations.set(model, modelValidations);
    }

    await this.performValidation();

    if (modelValidations.has(validator)) {
      return modelValidations.get(validator)!;
    }

    const promise = runValidator(model, validator, Binder.interpolateMessageCallback);
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
   * Reset the form to the previous value
   */
  reset(): void {
    this.read(this.#defaultValue);
  }

  /**
   * Submit the current form value to a predefined
   * onSubmit callback.
   *
   * It's a no-op if the onSubmit callback is undefined.
   */
  async submit(): Promise<unknown> {
    if (this.#onSubmit !== undefined) {
      return this.submitTo(this.#onSubmit);
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

    this.#submitting = true;
    this.update(this.value);
    try {
      return await endpointMethod.call(this.context, this.value);
    } catch (error: unknown) {
      if (isEndpointValidationError(error)) {
        const valueErrors: ReadonlyArray<ValueError<string | undefined>> = error.validationErrorData.map(
          (data: EndpointValidationErrorData) => {
            const res = endpointValidationErrorDataPattern.exec(data.message);
            const [property, value, message] = res ? res.splice(2) : [data.parameterName, undefined, data.message];
            return { message, property, validator: new ServerValidator(message), value };
          },
        );
        this.setErrorsWithDescendants(valueErrors);
        throw new ValidationError(valueErrors);
      }

      throw error;
    } finally {
      this.#submitting = false;
      this.defaultValue = this.value;
      this.update(this.value);
    }
  }

  protected completeValidation(): void {
    this.#validating = false;
  }

  protected performValidation(): Promise<void> | void {
    if (!this.#validationRequestSymbol) {
      this.#validating = true;
      this.#validationRequestSymbol = Promise.resolve().then(() => {
        this.#validationRequestSymbol = undefined;
      });
    }
    return this.#validationRequestSymbol;
  }

  protected override update(oldValue: T): void {
    this.#onChange?.call(this.context, oldValue);
  }
}
