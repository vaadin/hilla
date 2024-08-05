export class ValidationError extends Error {
  readonly value: unknown;

  constructor(name: string, value: unknown, message: string) {
    super(`[${name}]: ${message}`);
    this.value = value;
  }
}

export type ValidatableHTMLElement = HTMLElement &
  Pick<
    HTMLInputElement,
    | 'checkValidity'
    | 'max'
    | 'maxLength'
    | 'min'
    | 'minLength'
    | 'pattern'
    | 'required'
    | 'setCustomValidity'
    | 'type'
    | 'validity'
  >;

export class Validator {
  readonly name: string = 'Validator';

  // eslint-disable-next-line @typescript-eslint/class-methods-use-this
  bind(_element: ValidatableHTMLElement): void {
    // do nothing
  }

  error(value: unknown, message = 'Invalid value'): ValidationError {
    return new ValidationError(this.name, value, message);
  }

  // eslint-disable-next-line @typescript-eslint/class-methods-use-this
  validate(_value: unknown, _state: ValidityState | undefined): boolean {
    return true;
  }
}

export class Required extends Validator {
  override readonly name: string = 'Required';

  override bind(element: ValidatableHTMLElement): void {
    element.required = true;
  }

  override validate(value: unknown, state: ValidityState | undefined): boolean {
    return !state?.valueMissing && value != null;
  }

  override error(value: unknown, message = 'Must present'): ValidationError {
    return super.error(value, message);
  }
}

export class Pattern extends Validator {
  override readonly name: string = 'Pattern';

  readonly #pattern: RegExp;

  constructor(pattern: RegExp | string) {
    super();
    this.#pattern = typeof pattern === 'string' ? new RegExp(pattern, 'u') : pattern;
  }

  override bind(element: ValidatableHTMLElement): void {
    element.pattern = this.#pattern.source;
  }

  override validate(value: unknown, state: ValidityState | undefined): boolean {
    return !state?.patternMismatch && this.#pattern.test(String(value));
  }

  override error(value: unknown, message = `Must comply the pattern ${this.#pattern}`): ValidationError {
    return super.error(value, message);
  }
}

export class IsNumber extends Pattern {
  override readonly name: string = 'IsNumber';

  constructor() {
    super(/^[0-9.,]*$/u);
  }

  override bind(element: ValidatableHTMLElement): void {
    super.bind(element);
    element.type = 'number';
  }

  override validate(value: unknown, state: ValidityState | undefined): boolean {
    return super.validate(value, state) && isFinite(Number(value));
  }

  override error(value: unknown, message = 'Must be a number'): ValidationError {
    return super.error(value, message);
  }
}

export class Email extends Pattern {
  override readonly name: string = 'Email';

  constructor() {
    super(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/u);
  }

  override bind(element: ValidatableHTMLElement): void {
    super.bind(element);
    element.type = 'email';
  }

  override error(value: unknown): ValidationError {
    return super.error(value, 'Must be a well-formed email address');
  }
}

export class Null extends Validator {
  override readonly name: string = 'Null';

  override validate(value: unknown, _: ValidityState | undefined): boolean {
    return value == null;
  }

  override error(value: unknown, message = 'Must be null'): ValidationError {
    return super.error(value, message);
  }
}

export class NotNull extends Required {
  override readonly name: string = 'NotNull';

  override error(value: unknown, message = 'Must not be null'): ValidationError {
    return super.error(value, message);
  }
}

export class NotEmpty extends Required {
  override readonly name: string = 'NotEmpty';

  override validate(value: unknown, state: ValidityState | undefined): boolean {
    return super.validate(value, state) && (typeof value === 'string' || Array.isArray(value)) && value.length > 0;
  }

  override error(value: unknown, message = 'Must not be empty'): ValidationError {
    return super.error(value, message);
  }
}

export class NotBlank extends Required {
  override readonly name: string = 'NotBlank';

  override validate(value: unknown, state: ValidityState | undefined): boolean {
    return super.validate(value, state) && typeof value === 'string' && /\S/u.test(value);
  }

  override error(value: unknown, message = 'Must not be blank'): ValidationError {
    return super.error(value, message);
  }
}

export class Min extends IsNumber {
  readonly #min: number;

  constructor(min: number) {
    super();
    this.#min = min;
  }

  override bind(element: ValidatableHTMLElement): void {
    super.bind(element);
    element.min = String(this.#min);
  }

  override validate(value: unknown, state: ValidityState | undefined): boolean {
    return !state?.rangeUnderflow && super.validate(value, state) && Number(value) >= this.#min;
  }

  override error(value: unknown, message = `Must be greater than or equal to ${this.#min}`): ValidationError {
    return super.error(value, message);
  }
}

export class Max extends IsNumber {
  readonly #max: number;

  constructor(max: number) {
    super();
    this.#max = max;
  }

  override bind(element: ValidatableHTMLElement): void {
    super.bind(element);
    element.max = String(this.#max);
  }

  override validate(value: unknown, state: ValidityState | undefined): boolean {
    return !state?.rangeOverflow && super.validate(value, state) && Number(value) <= this.#max;
  }

  override error(value: unknown, message = `Must be less than or equal to ${this.#max}`): ValidationError {
    return super.error(value, message);
  }
}
