import { ConstraintDeclarationBuilder } from './builders.js';
import { ArrayModel, BooleanModel, NumberModel, RecordModel, StringModel } from './core.js';
import m from './m.js';
import { Model } from './model.js';

/**
 * The constrained model value must be `undefined`.
 */
export const Null = new ConstraintDeclarationBuilder(Model)
  .attribute('message', m.optional(StringModel))
  .name('Null')
  .build();

/**
 * The constrained model value must not be `undefined`.
 */
export const NotNull = new ConstraintDeclarationBuilder(Model)
  .attribute('message', m.optional(StringModel))
  .name('NotNull')
  .build();

/**
 * The constrained model value must be `true`.
 *
 * `undefined` value is considered valid.
 */
export const AssertTrue = new ConstraintDeclarationBuilder(BooleanModel)
  .attribute('message', m.optional(StringModel))
  .name('AssertTrue')
  .build();

/**
 * The constrained model value must be `false`.
 *
 * `undefined` value is considered valid.
 */
export const AssertFalse = new ConstraintDeclarationBuilder(BooleanModel)
  .attribute('message', m.optional(StringModel))
  .name('AssertFalse')
  .build();

/**
 * The constrained model value must be higher than or equal to the specified value.
 *
 * `undefined` value is considered valid.
 */
export const Min = new ConstraintDeclarationBuilder(NumberModel)
  .attribute('message', m.optional(StringModel))
  .name('Min')
  .attribute('value', NumberModel)
  .build();

/**
 * The constrained model value must be lower than or equal to the specified value.
 *
 * `undefined` value is considered valid.
 */
export const Max = new ConstraintDeclarationBuilder(NumberModel)
  .attribute('message', m.optional(StringModel))
  .name('Max')
  .attribute('value', NumberModel)
  .build();

/**
 * The constrained model value must be a number that is greater than or equal to the specified value.
 *
 * `undefined` value is considered valid.
 */
export const DecimalMin = new ConstraintDeclarationBuilder(m.union(NumberModel, StringModel))
  .name('DecimalMin')
  .attribute('value', NumberModel)
  .build();

/**
 * The constrained model value must be a number that is lower than or equal to the specified value.
 *
 * `undefined` value is considered valid.
 */
export const DecimalMax = new ConstraintDeclarationBuilder(m.union(NumberModel, StringModel))
  .name('DecimalMax')
  .build();

/**
 * The constrained model value must be strictly below zero.
 *
 * `undefined` value is considered valid.
 */
export const Negative = new ConstraintDeclarationBuilder(NumberModel)
  .attribute('message', m.optional(StringModel))
  .name('Negative');

/**
 * The constrained model value must be below zero or equal to zero.
 *
 * `undefined` value is considered valid.
 */
export const NegativeOrZero = new ConstraintDeclarationBuilder(NumberModel)
  .attribute('message', m.optional(StringModel))
  .name('NegativeOrZero');

/**
 * The constrained model value must be strictly above zero.
 *
 * `undefined` value is considered valid.
 */
export const Positive = new ConstraintDeclarationBuilder(NumberModel)
  .attribute('message', m.optional(StringModel))
  .name('Positive');

/**
 * The constrained model value must be above zero or equal to zero.
 *
 * `undefined` value is considered valid.
 */
export const PositiveOrZero = new ConstraintDeclarationBuilder(NumberModel)
  .attribute('message', m.optional(StringModel))
  .name('PositiveOrZero');

/**
 * The length of the constrained value must be between the specified boundaries.
 *
 * `undefined` value is considered valid.
 */
export const Size = new ConstraintDeclarationBuilder(m.union(StringModel, ArrayModel))
  .name('Size')
  .attribute('min', m.optional(m.withDefaultValue(NumberModel, 0)))
  .attribute('max', m.optional(m.withDefaultValue(NumberModel, Number.MAX_SAFE_INTEGER)))
  .build();

/**
 * The constrained value must be a number within the specified range.
 *
 * `undefined` value is considered valid.
 */
export const Digits = new ConstraintDeclarationBuilder(m.union(StringModel, NumberModel))
  .name('Digits')
  .attribute('integer', NumberModel)
  .attribute('fraction', NumberModel)
  .build();

/**
 * The constrained value must be a date, time, or timestamp in the past.
 *
 * `undefined` value is considered valid.
 */
export const Past = new ConstraintDeclarationBuilder(StringModel).name('Past').build();

/**
 * The constrained value must be a date, time, or timestamp in the past or present.
 *
 * `undefined` value is considered valid.
 */
export const PastOrPresent = new ConstraintDeclarationBuilder(StringModel).name('PastOrPresent').build();

/**
 * The constrained value must be a date, time, or timestamp in the future.
 *
 * `undefined` value is considered valid.
 */
export const Future = new ConstraintDeclarationBuilder(StringModel).name('Future').build();

/**
 * The constrained value must be a date, time, or timestamp in the future or present.
 *
 * `undefined` value is considered valid.
 */
export const FutureOrPresent = new ConstraintDeclarationBuilder(StringModel).name('FutureOrPresent').build();

/**
 * The constrained value must match the specified regular expression.
 *
 * `undefined` value is considered valid.
 */
export const Pattern = new ConstraintDeclarationBuilder(StringModel)
  .name('Pattern')
  .attribute('value', StringModel)
  .build();

/**
 * The constrained value must not be an `undefined` or empty.
 *
 * `undefined` value is considered valid.
 */
export const NotEmpty = new ConstraintDeclarationBuilder(m.union(StringModel, ArrayModel, RecordModel))
  .name('NotEmpty')
  .build();

/**
 * The constrained value must not be an `undefined` and must contain at least one non-whitespace character.
 *
 * `undefined` value is considered valid.
 */
export const NotBlank = new ConstraintDeclarationBuilder(StringModel).name('NotBlank').build();

/**
 * The constrained value must be a valid email address.
 *
 * `undefined` value is considered valid.
 */
export const Email = new ConstraintDeclarationBuilder(StringModel).name('Email').build();
