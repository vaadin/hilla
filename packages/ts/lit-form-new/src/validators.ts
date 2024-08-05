import { type Validator, $validator, $error, ValidationError } from '@vaadin/hilla-models';

export interface NativeValidationRules {
  required?: HTMLInputElement['required'];
  min?: HTMLInputElement['min'];
  max?: HTMLInputElement['max'];
  minLength?: HTMLInputElement['minLength'];
  maxLength?: HTMLInputElement['maxLength'];
  pattern?: HTMLInputElement['pattern'];
}

export interface RuleValidator extends NativeValidationRules, Validator {}

export interface CustomValidator extends Validator {
  (value: unknown): boolean;
}

export function createValidator(name: string, error: string, callback: (value: unknown) => boolean): CustomValidator;
export function createValidator(name: string, error: string, rules: NativeValidationRules): RuleValidator;
export function createValidator(
  name: string,
  error: string,
  validator: NativeValidationRules | ((value: unknown) => boolean),
): Validator {
  return Object.assign(validator, {
    [$validator]: name,
    [$error]: (value: unknown) => new ValidationError(value, error),
  }) as Validator;
}

export function isRuleValidator(obj: unknown): obj is RuleValidator {
  return typeof obj === 'object' && !!obj && $validator in obj;
}
export function isCustomValidator(obj: unknown): obj is CustomValidator {
  return typeof obj === 'function' && $validator in obj;
}
export function isValidator(obj: unknown): obj is Validator {
  return isRuleValidator(obj) || isCustomValidator(obj);
}

export const Required = createValidator('Required', 'Required', {
  required: true,
});

export const Pattern = (pattern: string): RuleValidator =>
  createValidator('Pattern', `Must match the following regular expression: ${String(pattern)}`, { pattern });

export const IsNumber = createValidator('IsNumber', 'Must be a number', Pattern('^[0-9]*$'));

export const Email = createValidator(
  'Email',
  'Must be a well-formed email address',
  Pattern('^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$'),
);

export const Null = createValidator('Null', 'Must be null', (value) => value == null);

export const NotNull = createValidator('NotNull', 'Must not be null', (value) => value != null);

export const NotEmpty = createValidator(
  'NotEmpty',
  'Must not be empty',
  (value) => NotNull(value) && (typeof value === 'string' || Array.isArray(value)) && value.length > 0,
);

export const NotBlank = createValidator(
  'NotBlank',
  'Must not be blank',
  (value) => typeof value === 'string' && /\S/u.test(value),
);

export const AssertTrue = createValidator('AssertTrue', 'Must be true', (value) => String(value) === 'true');

export const AssertFalse = createValidator('AssertFalse', 'Must be false', (value) => !AssertTrue(value));

export const Min = (min: number): RuleValidator =>
  createValidator('Min', `Must be greater than or equal to ${min}`, {
    minLength: min,
  });

export const Max = (max: number): RuleValidator =>
  createValidator('Max', `Must be less than or equal to ${max}`, {
    maxLength: max,
  });

export const DecimalMin = (min: number): RuleValidator =>
  createValidator('DecimalMin', `Must be greater than or equal to ${min}`, {
    min: String(min),
  });

export const DecimalMax = (max: number): RuleValidator =>
  createValidator('DecimalMax', `Must be less than or equal to ${max}`, {
    max: String(max),
  });

export const Negative = createValidator('Negative', 'Must be less than 0', (value) => Number(String(value)) < 0);

export const NegativeOrZero = createValidator(
  'NegativeOrZero',
  'Must be less than or equal to 0',
  (value) => Number(String(value)) <= 0,
);

export const Positive = createValidator('Positive', 'Must be greater than 0', (value) => Number(String(value)) > 0);

export const PositiveOrZero = createValidator(
  'PositiveOrZero',
  'Must be greater than or equal to 0',
  (value) => Number(String(value)) >= 0,
);

export const Size = (min: number, max: number): RuleValidator =>
  createValidator('Size', `Must be between ${min} and ${max}`, {
    minLength: min,
    maxLength: max,
  });

export const Digits = (integer: number, fraction: number): RuleValidator =>
  createValidator(
    'Digits',
    `Must be a number with up to ${integer} digits and up to ${fraction} decimals`,
    Pattern(`^[0-9]{1,${integer}}[.,][0-9]{0,${fraction}$`),
  );
