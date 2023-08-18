import isAfter from 'validator/es/lib/isAfter.js';
import isBefore from 'validator/es/lib/isBefore.js';
import isBoolean from 'validator/es/lib/isBoolean.js';
import isDecimal from 'validator/es/lib/isDecimal.js';
import isEmail from 'validator/es/lib/isEmail.js';
import isFloat from 'validator/es/lib/isFloat.js';
import isLength from 'validator/es/lib/isLength.js';
import isNumeric from 'validator/es/lib/isNumeric.js';
import matches from 'validator/es/lib/matches.js';
import toFloat from 'validator/es/lib/toFloat.js';
import type { Validator } from './Validation.js';

interface ValidatorAttributes {
  message?: string;
}
interface ValueNumberAttributes extends ValidatorAttributes {
  value: number | string;
}
interface DigitAttributes extends ValidatorAttributes {
  integer: number;
  fraction: number;
}
interface SizeAttributes extends ValidatorAttributes {
  min?: number;
  max?: number;
}
interface PatternAttributes extends ValidatorAttributes {
  regexp: RegExp | string;
}
interface DecimalAttributes extends ValueNumberAttributes {
  inclusive?: boolean;
}

abstract class AbstractValidator<T> implements Validator<T> {
  message = 'invalid';

  impliesRequired = false;

  constructor(attrs?: ValidatorAttributes) {
    if (attrs?.message) {
      this.message = attrs.message;
    }
  }

  abstract validate(value: T): Promise<boolean> | boolean;
}

export class Required<T> extends AbstractValidator<T> {
  override impliesRequired = true;

  override validate(value: T): boolean {
    if (typeof value === 'string' || Array.isArray(value)) {
      return value.length > 0;
    }
    if (typeof value === 'number') {
      return Number.isFinite(value);
    }
    return value !== undefined;
  }
}

function _asValidatorAttributes(attrs: PatternAttributes | RegExp | ValueNumberAttributes | number | string) {
  return typeof attrs === 'object' ? attrs : {};
}

function _value(attrs: ValueNumberAttributes | number | string) {
  return typeof attrs === 'object' ? attrs.value : attrs;
}

abstract class NumberValidator<T> extends AbstractValidator<T> {
  override validate(value: T) {
    return isNumeric(String(value));
  }
}

export class IsNumber extends NumberValidator<number | null | undefined> {
  optional: boolean;

  constructor(optional: boolean, attrs?: ValidatorAttributes) {
    super({ message: 'must be a number', ...attrs });
    this.optional = optional;
  }

  override validate(value: number | null | undefined): boolean {
    return (this.optional && value == null) || super.validate(value);
  }
}

abstract class ValueNumberValidator<T> extends NumberValidator<T> {
  value: number;

  protected constructor(attrs: ValueNumberAttributes | number | string) {
    super(_asValidatorAttributes(attrs));
    const val = _value(attrs);
    this.value = typeof val === 'string' ? parseFloat(val) : val;
  }
}

// JSR380 equivalent (https://beanvalidation.org/2.0/spec/#builtinconstraints)
export class Email extends AbstractValidator<string> {
  constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must be a well-formed email address', ...attrs });
  }

  override validate(value: string | null | undefined): boolean {
    return !value || isEmail(value);
  }
}
export class Null extends AbstractValidator<any> {
  constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must be null', ...attrs });
  }

  override validate(value: any): boolean {
    return value == null;
  }
}
export class NotNull<T> extends Required<T> {
  constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must not be null', ...attrs });
  }

  override validate(value: T): value is NonNullable<T> {
    return !new Null().validate(value);
  }
}
export class NotEmpty<T> extends Required<T> {
  constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must not be empty', ...attrs });
  }

  override validate(value: T): boolean {
    return (
      super.validate(value) && new NotNull<T>().validate(value) && ((value as { length?: number }).length ?? 0) > 0
    );
  }
}
export class NotBlank<T> extends Required<T> {
  constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must not be blank', ...attrs });
  }

  override validate(value: T): boolean {
    return super.validate(value) && new NotNull<T>().validate(value) && String(value).trim().length > 0;
  }
}
export class AssertTrue<T> extends AbstractValidator<T> {
  constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must be true', ...attrs });
  }

  override validate(value: T): boolean {
    return isBoolean(String(value)) && String(value) === 'true';
  }
}
export class AssertFalse<T> extends AbstractValidator<T> {
  constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must be false', ...attrs });
  }

  override validate(value: T): boolean {
    return !new AssertTrue<T>().validate(value);
  }
}

function _asValueNumberAttributes(attrs: ValueNumberAttributes | number | string) {
  return typeof attrs === 'object' ? attrs : { value: attrs };
}

export class Min<T> extends ValueNumberValidator<T> {
  constructor(attrs: ValueNumberAttributes | number | string) {
    super({
      message: `must be greater than or equal to ${_value(attrs)}`,
      ..._asValueNumberAttributes(attrs),
    });
  }

  override validate(value: T): boolean {
    return super.validate(value) && isFloat(String(value), { min: this.value });
  }
}
export class Max<T> extends ValueNumberValidator<T> {
  constructor(attrs: ValueNumberAttributes | number | string) {
    super({
      message: `must be less than or equal to ${_value(attrs)}`,
      ..._asValueNumberAttributes(attrs),
    });
  }

  override validate(value: T): boolean {
    return super.validate(value) && isFloat(String(value), { max: this.value });
  }
}

function _inclusive(attrs: DecimalAttributes | number | string) {
  return typeof attrs !== 'object' || attrs.inclusive !== false;
}

export class DecimalMin<T> extends ValueNumberValidator<T> {
  inclusive: boolean;

  constructor(attrs: DecimalAttributes | number | string) {
    super({
      message: `must be greater than ${_inclusive(attrs) ? 'or equal to ' : ''}${_value(attrs)}`,
      ..._asValueNumberAttributes(attrs),
    });
    this.inclusive = _inclusive(attrs);
  }

  override validate(value: T): boolean {
    return super.validate(value) && isFloat(String(value), { [this.inclusive ? 'min' : 'gt']: this.value });
  }
}
export class DecimalMax<T> extends ValueNumberValidator<T> {
  inclusive: boolean;

  constructor(attrs: DecimalAttributes | number | string) {
    super({
      message: `must be less than ${_inclusive(attrs) ? 'or equal to ' : ''}${_value(attrs)}`,
      ..._asValueNumberAttributes(attrs),
    });
    this.inclusive = _inclusive(attrs);
  }

  override validate(value: T): boolean {
    return super.validate(value) && isFloat(String(value), { [this.inclusive ? 'max' : 'lt']: this.value });
  }
}
export class Negative<T> extends AbstractValidator<T> {
  constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must be less than 0', ...attrs });
  }

  override validate(value: T): boolean {
    return toFloat(String(value)) < 0;
  }
}
export class NegativeOrZero<T> extends AbstractValidator<T> {
  constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must be less than or equal to 0', ...attrs });
  }

  override validate(value: T): boolean {
    return toFloat(String(value)) <= 0;
  }
}
export class Positive<T> extends AbstractValidator<T> {
  constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must be greater than 0', ...attrs });
  }

  override validate(value: T): boolean {
    return toFloat(String(value)) > 0;
  }
}
export class PositiveOrZero<T> extends AbstractValidator<T> {
  constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must be greater than or equal to 0', ...attrs });
  }

  override validate(value: T): boolean {
    return toFloat(String(value)) >= 0;
  }
}

function _min(attrs: SizeAttributes) {
  return attrs.min ?? 0;
}

function _max(attrs: SizeAttributes) {
  return attrs.max ?? Number.MAX_SAFE_INTEGER;
}

export class Size extends AbstractValidator<string> {
  min: number;

  max: number;

  constructor(attrs: SizeAttributes = {}) {
    super({ message: `size must be between ${_min(attrs)} and ${_max(attrs)}`, ...attrs });
    this.min = _min(attrs);
    this.max = _max(attrs);
    if (this.min > 0) {
      this.impliesRequired = true;
    }
  }

  override validate(value: string): boolean {
    if (this.min && this.min > 0 && !new Required().validate(value)) {
      return false;
    }
    // eslint-disable-next-line sort-keys
    return isLength(value, { min: this.min, max: this.max });
  }
}

export class Digits<T> extends AbstractValidator<T> {
  integer: number;

  fraction: number;

  constructor(attrs: DigitAttributes) {
    super({
      message: `numeric value out of bounds (<${attrs.integer} digits>.<${attrs.fraction} digits> expected)`,
      ...attrs,
    });
    this.integer = attrs.integer;
    this.fraction = attrs.fraction;
  }

  override validate(value: T): boolean {
    return (
      String(Math.floor(Math.abs(toFloat(String(value))))).length <= this.integer &&
      // eslint-disable-next-line camelcase
      isDecimal(String(value), { decimal_digits: `0,${this.fraction}` })
    );
  }
}

export class Past<T> extends AbstractValidator<T> {
  constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must be a past date', ...attrs });
  }

  override validate(value: T): boolean {
    return isBefore(String(value));
  }
}
/*
  @PastOrPresent has no client-side implementation yet.
  It would consider any input valid and let the server-side to do validation.
  (It's not trivial to ensure the same granularity of _present_ as on the server-side:
  year / month / day / minute).
*/
// export class PastOrPresent extends AbstractValidator<any> {
//   constructor(attrs?: ValidatorAttributes) {
//     super({ message: 'must be a date in the past or in the present', ...attrs });
//   }
//   validate() {
//     return true;
//   }
// }
export class Future<T> extends AbstractValidator<T> {
  constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must be a future date', ...attrs });
  }

  override validate(value: T): boolean {
    return isAfter(String(value));
  }
}

/*
  @FutureOrPresent has no client-side implementation yet.
  It would consider any input valid and let the server-side to do validation.
  (It's not trivial to ensure the same granularity of _present_ as on the server-side:
  year / month / day / minute).
*/
// export class FutureOrPresent extends AbstractValidator<any> {
//   constructor(attrs?: ValidatorAttributes) {
//     super({ message: 'must be a date in the present or in the future', ...attrs });
//   }
//   validate = () => true;
// }

function _regexp(attrs: PatternAttributes | RegExp | string) {
  if (typeof attrs === 'string') {
    return new RegExp(attrs, 'u');
  }

  if (attrs instanceof RegExp) {
    return attrs;
  }

  if (typeof attrs.regexp === 'string') {
    return new RegExp(attrs.regexp, 'u');
  }

  return attrs.regexp;
}

export class Pattern extends AbstractValidator<string> {
  regexp: RegExp;

  constructor(attrs: PatternAttributes | RegExp | string) {
    super({
      message: `must match the following regular expression: ${_regexp(attrs).toString()}`,
      ..._asValidatorAttributes(attrs),
    });
    this.regexp = _regexp(attrs);
  }

  override validate(value: any): boolean {
    return matches(value, this.regexp);
  }
}

/**
 * Validator that reports an error when the bound HTML element validation
 * returns false from `element.checkValidity()` and `element.validity.valid`.
 */
export class ValidityStateValidator<T> extends AbstractValidator<T> {
  override message = '';

  // eslint-disable-next-line no-useless-constructor,@typescript-eslint/no-useless-constructor
  constructor() {
    super();
  }

  override validate(): boolean {
    return false;
  }
}
