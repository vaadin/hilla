const { create, entries, getPrototypeOf, getOwnPropertyDescriptors } = Object;

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

export interface Validator {
  name: string;
  super: Validator;
  error(value: unknown): ValidationError;
  validate(value: unknown, state?: ValidityState): boolean;
  bind(element: ValidatableHTMLElement): void;
}

export type ValidatorProps = Partial<Omit<Validator, 'error'> & { error: string }>;

export function createValidator(name: string, base: Validator, props: ValidatorProps): Validator {
  return create(
    base,
    entries(getOwnPropertyDescriptors(props)).reduce<Record<string, PropertyDescriptor>>(
      (acc, [key, { value, get, set }]) => {
        acc[key] =
          key === 'error' ? { value: (v: unknown) => new ValidationError(name, v, value) } : { value, get, set };
        return acc;
      },
      { name: { value: name } },
    ),
  );
}

export const Validator = create(null, {
  name: {
    value: 'Validator',
  },
  bind: {
    value: () => {},
  },
  error: {
    value(this: Validator) {
      return new ValidationError(this.name, undefined, 'Invalid value');
    },
  },
  validate: {
    value: () => true,
  },
  super: {
    get(this: Validator) {
      return getPrototypeOf(this);
    },
  },
  [Symbol.hasInstance]: {
    value(this: Validator, o: unknown) {
      return typeof o === 'object' && o != null && (this === o || Object.prototype.isPrototypeOf.call(this, o));
    },
  },
});

export const Required = createValidator('Required', Validator, {
  bind(element) {
    element.required = true;
  },
  validate: (value, state) => !state?.valueMissing && value != null,
});

export function Pattern(pattern: RegExp | string): Validator {
  const _pattern = pattern instanceof RegExp ? pattern : new RegExp(pattern, 'u');

  return createValidator('Pattern', Validator, {
    bind(element) {
      element.pattern = _pattern.source;
    },
    validate: (value, state) => !state?.patternMismatch && _pattern.test(String(value)),
  });
}

export const IsNumber = createValidator('IsNumber', Pattern(/^[0-9]*$/u), {
  bind(element) {
    element.type = 'number';
  },
  validate(this: Validator, value, state) {
    return !state?.typeMismatch && this.super.validate(value) && isFinite(Number(value));
  },
});

export const Email = createValidator('Email', Pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/u), {
  error: 'Must be a well-formed email address',
});

export const Null = createValidator('Null', Validator, {
  validate: (value) => value == null,
  error: 'Must be null',
});

export const NotNull = createValidator('NotNull', Required, {
  error: 'Must not be null',
});

export const NotEmpty = createValidator('NotEmpty', Required, {
  validate(this: Validator, value) {
    return this.super.validate(value) && (typeof value === 'string' || Array.isArray(value)) && value.length > 0;
  },
  error: 'Must not be empty',
});

export const NotBlank = createValidator('NotBlank', Required, {
  validate(this: Validator, value) {
    return this.super.validate(value) && typeof value === 'string' && /\S/u.test(value);
  },
  error: 'Must not be blank',
});

export function Min(min: number): Validator {
  return createValidator('Min', IsNumber, {
    validate: (value, state) => !state?.rangeUnderflow && IsNumber.validate(value) && Number(value) >= min,
    bind(element) {
      element.min = String(min);
    },
    error: `Must be greater than or equal to ${min}`,
  });
}

export function Max(max: number): Validator {
  return createValidator('Max', IsNumber, {
    validate: (value, state) => !state?.rangeOverflow && IsNumber.validate(value) && Number(value) <= max,
    bind(element) {
      element.max = String(max);
    },
    error: `Must be less than or equal to ${max}`,
  });
}
