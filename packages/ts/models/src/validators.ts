const { create, defineProperty, getPrototypeOf } = Object;

const $validator = Symbol('validator');

export class ValidationError extends Error {
  readonly value: unknown;

  constructor(name: string, value: unknown, message: string) {
    super(`[${name}]: ${message}`);
    this.value = value;
  }
}

export type ConstrainedHTMLElement = HTMLElement &
  Pick<HTMLInputElement, 'max' | 'maxLength' | 'min' | 'minLength' | 'pattern' | 'required'>;

export interface Validator {
  brand: typeof $validator;
  name: string;
  error(value: unknown): ValidationError;
  validate(value: unknown): boolean;
  applyToElement(element: ConstrainedHTMLElement): void;
}

export function isValidator(obj: unknown): obj is Validator {
  return typeof obj === 'object' && !!obj && 'brand' in obj && obj.brand === $validator;
}

export const Validator = create(null, {
  brand: {
    value: $validator,
  },
  name: {
    value: 'Validator',
  },
  applyToElement: {
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
});

export class ValidatorBuilder {
  readonly #validator: Validator;

  constructor(name: string, base: Validator) {
    this.#validator = create(base, {
      name: {
        value: name,
      },
    });
  }

  define<K extends keyof Validator>(property: K, descriptor: PropertyDescriptor): this {
    defineProperty(this.#validator, property, descriptor);
    return this;
  }

  error(error: string): this {
    return this.define('error', {
      value(this: Validator, value: unknown) {
        return new ValidationError(this.name, value, error);
      },
    });
  }

  validate(validate: (value: unknown, super_: Validator) => boolean): this {
    return this.define('validate', {
      value(this: Validator, value: unknown) {
        return validate(value, getPrototypeOf(this));
      },
    });
  }

  applyToElement(applyToElement: (element: ConstrainedHTMLElement, base: Validator) => void): this {
    return this.define('applyToElement', {
      value(this: Validator, element: ConstrainedHTMLElement) {
        applyToElement(element, getPrototypeOf(this));
      },
    });
  }

  build(): Validator {
    return this.#validator;
  }
}

export const Required = new ValidatorBuilder('Required', Validator)
  .applyToElement((element) => {
    element.required = true;
  })
  .validate((value) => value != null)
  .build();

export function Pattern(pattern: RegExp | string): Validator {
  const _pattern = pattern instanceof RegExp ? pattern : new RegExp(pattern, 'u');
  return new ValidatorBuilder('Pattern', Validator)
    .error(`Must match the following regular expression: ${String(pattern)}`)
    .validate((value) => _pattern.test(String(value)))
    .applyToElement((element) => {
      element.pattern = _pattern.source;
    })
    .build();
}

export const IsNumber = new ValidatorBuilder('IsNumber', Pattern('^[0-9]*$'))
  .validate((value, super_) => super_.validate(value) && isFinite(Number(value)))
  .error('Must be a number')
  .build();

export const Email = new ValidatorBuilder('Email', Pattern('^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$'))
  .error('Must be a well-formed email address')
  .build();

export const Null = new ValidatorBuilder('Null', Validator)
  .validate((value) => value == null)
  .error('Must be null')
  .build();

export const NotNull = new ValidatorBuilder('NotNull', Required).error('Must not be null').build();

export const NotEmpty = new ValidatorBuilder('NotEmpty', Required)
  .validate(
    (value, super_) =>
      super_.validate(value) && (typeof value === 'string' || Array.isArray(value)) && value.length > 0,
  )
  .error('Must not be empty')
  .build();

export const NotBlank = new ValidatorBuilder('NotBlank', Required)
  .validate((value, super_) => super_.validate(value) && typeof value === 'string' && /\S/u.test(value))
  .error('Must not be blank')
  .build();

export function Min(min: number): Validator {
  return new ValidatorBuilder('Min', IsNumber)
    .validate((value, super_) => super_.validate(value) && Number(value) >= min)
    .applyToElement((element) => {
      element.minLength = min;
    })
    .error(`Must be greater than or equal to ${min}`)
    .build();
}

export function Max(max: number): Validator {
  return new ValidatorBuilder('Max', IsNumber)
    .validate((value, super_) => super_.validate(value) && Number(value) <= max)
    .applyToElement((element) => {
      element.maxLength = max;
    })
    .error(`Must be less than or equal to ${max}`)
    .build();
}
