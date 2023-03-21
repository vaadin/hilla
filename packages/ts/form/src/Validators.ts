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
  public message = 'invalid';

  public impliesRequired = false;

  public constructor(attrs?: ValidatorAttributes) {
    if (attrs && attrs.message) {
      this.message = attrs.message;
    }
  }

  public abstract validate(value: T): boolean | Promise<boolean>;
}

export class Required<T> extends AbstractValidator<T> {
  public override impliesRequired = true;

  public override validate(value: T) {
    if (typeof value === 'string' || Array.isArray(value)) {
      return value.length > 0;
    }
    if (typeof value === 'number') {
      return Number.isFinite(value);
    }
    return value !== undefined;
  }
}

function _asValidatorAttributes(attrs: ValueNumberAttributes | number | string | PatternAttributes | RegExp) {
  return typeof attrs === 'object' ? attrs : {};
}

function _value(attrs: ValueNumberAttributes | number | string) {
  return typeof attrs === 'object' ? attrs.value : attrs;
}

abstract class NumberValidator<T> extends AbstractValidator<T> {
  public override validate(value: T) {
    return isNumeric(String(value));
  }
}

export class IsNumber extends NumberValidator<number | null | undefined> {
  public optional: boolean;

  public constructor(optional: boolean, attrs?: ValidatorAttributes) {
    super({ message: 'must be a number', ...attrs });
    this.optional = optional;
  }

  public override validate(value: number | null | undefined) {
    return (this.optional && value == null) || super.validate(value);
  }
}

abstract class ValueNumberValidator<T> extends NumberValidator<T> {
  public value: number;

  protected constructor(attrs: ValueNumberAttributes | number | string) {
    super(_asValidatorAttributes(attrs));
    const val = _value(attrs);
    this.value = typeof val === 'string' ? parseFloat(val) : val;
  }
}

// JSR380 equivalent (https://beanvalidation.org/2.0/spec/#builtinconstraints)
export class Email extends AbstractValidator<string> {
  public constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must be a well-formed email address', ...attrs });
  }

  public override validate(value: string | null | undefined) {
    return !value || isEmail(value);
  }
}
export class Null extends AbstractValidator<any> {
  public constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must be null', ...attrs });
  }

  public override validate(value: any) {
    return value == null;
  }
}
export class NotNull extends Required<any> {
  public constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must not be null', ...attrs });
  }

  public override validate(value: any) {
    return !new Null().validate(value);
  }
}
export class NotEmpty extends Required<any> {
  public constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must not be empty', ...attrs });
  }

  public override validate(value: any) {
    return super.validate(value) && new NotNull().validate(value) && value.length > 0;
  }
}
export class NotBlank extends Required<any> {
  public constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must not be blank', ...attrs });
  }

  public override validate(value: any) {
    return super.validate(value) && new NotNull().validate(value) && value.trim().length > 0;
  }
}
export class AssertTrue extends AbstractValidator<any> {
  public constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must be true', ...attrs });
  }

  public override validate(value: any) {
    return isBoolean(String(value)) && String(value) === 'true';
  }
}
export class AssertFalse extends AbstractValidator<any> {
  public constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must be false', ...attrs });
  }

  public override validate(value: any) {
    return !new AssertTrue().validate(value);
  }
}

function _asValueNumberAttributes(attrs: ValueNumberAttributes | number | string) {
  return typeof attrs === 'object' ? attrs : { value: attrs };
}

export class Min extends ValueNumberValidator<any> {
  public constructor(attrs: ValueNumberAttributes | number | string) {
    super({
      message: `must be greater than or equal to ${_value(attrs)}`,
      ..._asValueNumberAttributes(attrs),
    });
  }

  public override validate(value: any) {
    return super.validate(value) && isFloat(String(value), { min: this.value });
  }
}
export class Max extends ValueNumberValidator<any> {
  public constructor(attrs: ValueNumberAttributes | number | string) {
    super({
      message: `must be less than or equal to ${_value(attrs)}`,
      ..._asValueNumberAttributes(attrs),
    });
  }

  public override validate(value: any) {
    return super.validate(value) && isFloat(String(value), { max: this.value });
  }
}

function _inclusive(attrs: DecimalAttributes | string | number) {
  return typeof attrs !== 'object' || attrs.inclusive !== false;
}

export class DecimalMin extends ValueNumberValidator<any> {
  public inclusive: boolean;

  public constructor(attrs: DecimalAttributes | string | number) {
    super({
      message: `must be greater than ${_inclusive(attrs) ? 'or equal to ' : ''}${_value(attrs)}`,
      ..._asValueNumberAttributes(attrs),
    });
    this.inclusive = _inclusive(attrs);
  }

  public override validate(value: any) {
    return super.validate(value) && isFloat(String(value), { [this.inclusive ? 'min' : 'gt']: this.value });
  }
}
export class DecimalMax extends ValueNumberValidator<any> {
  public inclusive: boolean;

  public constructor(attrs: DecimalAttributes | string | number) {
    super({
      message: `must be less than ${_inclusive(attrs) ? 'or equal to ' : ''}${_value(attrs)}`,
      ..._asValueNumberAttributes(attrs),
    });
    this.inclusive = _inclusive(attrs);
  }

  public override validate(value: any) {
    return super.validate(value) && isFloat(String(value), { [this.inclusive ? 'max' : 'lt']: this.value });
  }
}
export class Negative extends AbstractValidator<any> {
  public constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must be less than 0', ...attrs });
  }

  public override validate(value: any) {
    return toFloat(`${value}`) < 0;
  }
}
export class NegativeOrZero extends AbstractValidator<any> {
  public constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must be less than or equal to 0', ...attrs });
  }

  public override validate(value: any) {
    return toFloat(`${value}`) <= 0;
  }
}
export class Positive extends AbstractValidator<any> {
  public constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must be greater than 0', ...attrs });
  }

  public override validate(value: any) {
    return toFloat(`${value}`) > 0;
  }
}
export class PositiveOrZero extends AbstractValidator<any> {
  public constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must be greater than or equal to 0', ...attrs });
  }

  public override validate(value: any) {
    return toFloat(`${value}`) >= 0;
  }
}

function _min(attrs: SizeAttributes) {
  return attrs.min || 0;
}

function _max(attrs: SizeAttributes) {
  return attrs.max || Number.MAX_SAFE_INTEGER;
}

export class Size extends AbstractValidator<string> {
  public min: number;

  public max: number;

  public constructor(attrs: SizeAttributes = {}) {
    super({ message: `size must be between ${_min(attrs)} and ${_max(attrs)}`, ...attrs });
    this.min = _min(attrs);
    this.max = _max(attrs);
    if (this.min > 0) {
      this.impliesRequired = true;
    }
  }

  public override validate(value: string) {
    if (this.min && this.min > 0 && !new Required().validate(value)) {
      return false;
    }
    return isLength(value, { min: this.min, max: this.max });
  }
}

export class Digits extends AbstractValidator<string> {
  public integer: number;

  public fraction: number;

  public constructor(attrs: DigitAttributes) {
    super({
      message: `numeric value out of bounds (<${attrs.integer} digits>.<${attrs.fraction} digits> expected)`,
      ...attrs,
    });
    this.integer = attrs.integer;
    this.fraction = attrs.fraction;
  }

  public override validate(value: any) {
    return (
      String(toFloat(`${value}`)).replace(/(.*)\.\d+/, '$1').length <= this.integer &&
      isDecimal(`${value}`, { decimal_digits: `0,${this.fraction}` })
    );
  }
}

export class Past extends AbstractValidator<any> {
  public constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must be a past date', ...attrs });
  }

  public override validate(value: any) {
    return isBefore(value);
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
export class Future extends AbstractValidator<any> {
  public constructor(attrs?: ValidatorAttributes) {
    super({ message: 'must be a future date', ...attrs });
  }

  public override validate(value: any) {
    return isAfter(value);
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

function _regexp(attrs: PatternAttributes | string | RegExp) {
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
  public regexp: RegExp;

  public constructor(attrs: PatternAttributes | string | RegExp) {
    super({
      message: `must match the following regular expression: ${_regexp(attrs)}`,
      ..._asValidatorAttributes(attrs),
    });
    this.regexp = _regexp(attrs);
  }

  public override validate(value: any) {
    return matches(value, this.regexp);
  }
}

/**
 * Validator that reports an error when the bound HTML element validation
 * returns false from `element.checkValidity()` and `element.validity.valid`.
 */
export class ValidityStateValidator<T> extends AbstractValidator<T> {
  public override message = '';

  // eslint-disable-next-line no-useless-constructor
  public constructor() {
    super();
  }

  public override validate(): boolean {
    return false;
  }
}
