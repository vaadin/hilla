import { assert } from '@open-wc/testing';
// API to test
import {
  AssertFalse,
  AssertTrue,
  DecimalMax,
  DecimalMin,
  Digits,
  Email,
  Future,
  IsNumber,
  Max,
  Min,
  Negative,
  NegativeOrZero,
  NotBlank,
  NotEmpty,
  NotNull,
  Null,
  Past,
  Pattern,
  Positive,
  PositiveOrZero,
  Required,
  Size,
} from '../src';

describe('form/Validators', () => {
  it('custom error message', () => {
    assert.equal(
      new Size({ min: 1, max: 2 }).message,
      'size must be between 1 and 2',
      'when no custom error message is provided, the default error message should be used',
    );
    assert.equal(
      new Size({ min: 1, max: 2, message: 'Please enter something with the size between 1 and 2' }).message,
      'Please enter something with the size between 1 and 2',
      'when a custom error message is provided, it should be used instead of the default one',
    );
  });

  it('Required', () => {
    const validator = new Required();
    assert.isTrue(validator.impliesRequired);
    assert.isTrue(validator.validate('foo'));
    assert.isFalse(validator.validate(''));
    assert.isFalse(validator.validate(undefined));
    assert.isTrue(validator.validate(0));
  });

  it('Email', () => {
    const validator = new Email();
    assert.isNotTrue(validator.impliesRequired);
    assert.isTrue(validator.validate(undefined));
    assert.isTrue(validator.validate(null));
    assert.isTrue(validator.validate('foo@vaadin.com'));
    assert.isTrue(validator.validate(''));
    assert.isFalse(validator.validate('foo'));
    assert.isFalse(validator.validate('foo@vaadin.c'));
    assert.isFalse(validator.validate('ñññ@vaadin.c'));
  });

  it('Null', () => {
    const validator = new Null();
    assert.isNotTrue(validator.impliesRequired);
    assert.isTrue(validator.validate(null));
    assert.isTrue(validator.validate(undefined));
    assert.isFalse(validator.validate(''));
  });

  it('NotNull', () => {
    const validator = new NotNull();
    assert.isTrue(validator.impliesRequired);
    assert.isTrue(validator.validate(''));
    assert.isFalse(validator.validate(null));
    assert.isFalse(validator.validate(undefined));
  });

  it('NotEmpty', () => {
    const validator = new NotEmpty();
    assert.isTrue(validator.impliesRequired);
    assert.isTrue(validator.validate('a'));
    assert.isTrue(validator.validate(['a']));
    assert.isFalse(validator.validate(''));
    assert.isFalse(validator.validate(undefined));
    assert.isFalse(validator.validate([]));
  });

  it('NotBlank', () => {
    const validator = new NotBlank();
    assert.isTrue(validator.impliesRequired);
    assert.isTrue(validator.validate('a'));
    assert.isFalse(validator.validate(''));
    assert.isFalse(validator.validate(undefined));
  });

  it('AssertTrue', () => {
    const validator = new AssertTrue();
    assert.isNotTrue(validator.impliesRequired);
    assert.isTrue(validator.validate('true'));
    assert.isTrue(validator.validate(true));
    assert.isFalse(validator.validate('a'));
    assert.isFalse(validator.validate(false));
    assert.isFalse(validator.validate('false'));
    assert.isFalse(validator.validate(''));
    assert.isFalse(validator.validate(undefined));
    assert.isFalse(validator.validate(null));
    assert.isFalse(validator.validate(1));
    assert.isFalse(validator.validate(0));
  });

  it('AssertFalse', () => {
    const validator = new AssertFalse();
    assert.isNotTrue(validator.impliesRequired);
    assert.isTrue(validator.validate('false'));
    assert.isTrue(validator.validate(false));
    assert.isTrue(validator.validate('a'));
    assert.isTrue(validator.validate('foo'));
    assert.isTrue(validator.validate(''));
    assert.isTrue(validator.validate(undefined));
    assert.isTrue(validator.validate(null));
    assert.isFalse(validator.validate('true'));
    assert.isFalse(validator.validate(true));
  });

  it('IsNumber', () => {
    let validator = new IsNumber(false);
    assert.isNotTrue(validator.impliesRequired);
    assert.isTrue(validator.validate(0));
    assert.isTrue(validator.validate(1));
    assert.isTrue(validator.validate(1.2));
    assert.isTrue(validator.validate(-0.5));
    assert.isFalse(validator.validate(Infinity));
    assert.isFalse(validator.validate(-Infinity));
    assert.isFalse(validator.validate(NaN));
    assert.isFalse(validator.validate(undefined));

    validator = new IsNumber(true);
    assert.isTrue(validator.validate(undefined));
  });

  it('Min', () => {
    let validator = new Min(1);
    assert.isNotTrue(validator.impliesRequired);
    assert.isTrue(validator.validate(1));
    assert.isTrue(validator.validate(1.1));
    assert.isFalse(validator.validate(0.9));
    validator = new Min({ message: 'foo', value: 1 });
    assert.isTrue(validator.validate(1));
    assert.isTrue(validator.validate(1.1));
    assert.isFalse(validator.validate(0.9));
  });

  it('Max', () => {
    const validator = new Max(1);
    assert.isNotTrue(validator.impliesRequired);
    assert.isTrue(validator.validate(1));
    assert.isTrue(validator.validate(0.9));
    assert.isFalse(validator.validate(1.1));
  });

  it('DecimalMin', () => {
    let validator = new DecimalMin('30.1');
    assert.isNotTrue(validator.impliesRequired);
    assert.isFalse(validator.validate(1));
    assert.isTrue(validator.validate(30.1));
    assert.isTrue(validator.validate(30.2));
    assert.isTrue(validator.validate('30.2'));
    validator = new DecimalMin({ value: '30.1', inclusive: false });
    assert.isFalse(validator.validate(30.1));
  });

  it('DecimalMax', () => {
    let validator = new DecimalMax('30.1');
    assert.isNotTrue(validator.impliesRequired);
    assert.isTrue(validator.validate(30));
    assert.isTrue(validator.validate(30.1));
    assert.isFalse(validator.validate(30.2));
    validator = new DecimalMin({ value: '30.1', inclusive: false });
    assert.isFalse(validator.validate(30.1));
  });

  it('Negative', () => {
    const validator = new Negative();
    assert.isNotTrue(validator.impliesRequired);
    assert.isTrue(validator.validate(-1));
    assert.isTrue(validator.validate(-0.01));
    assert.isFalse(validator.validate(0));
    assert.isFalse(validator.validate(1));
  });

  it('NegativeOrZero', () => {
    const validator = new NegativeOrZero();
    assert.isNotTrue(validator.impliesRequired);
    assert.isTrue(validator.validate(-1));
    assert.isTrue(validator.validate(-0.01));
    assert.isTrue(validator.validate(0));
    assert.isFalse(validator.validate(1));
  });

  it('Positive', () => {
    const validator = new Positive();
    assert.isNotTrue(validator.impliesRequired);
    assert.isFalse(validator.validate(-1));
    assert.isFalse(validator.validate(-0.01));
    assert.isFalse(validator.validate(0));
    assert.isTrue(validator.validate(0.01));
  });

  it('PositiveOrZero', () => {
    const validator = new PositiveOrZero();
    assert.isNotTrue(validator.impliesRequired);
    assert.isFalse(validator.validate(-1));
    assert.isFalse(validator.validate(-0.01));
    assert.isTrue(validator.validate(0));
    assert.isTrue(validator.validate(0.01));
  });

  it('Size', () => {
    const validator = new Size({ min: 2, max: 4 });
    assert.isTrue(validator.impliesRequired);
    assert.isFalse(validator.validate(''));
    assert.isFalse(validator.validate('a'));
    assert.isTrue(validator.validate('aa'));
    assert.isTrue(validator.validate('aaa'));
    const noMinValidator = new Size({ max: 3 });
    assert.isNotTrue(noMinValidator.impliesRequired);
    const minZeroValidator = new Size({ min: 0, max: 3 });
    assert.isNotTrue(minZeroValidator.impliesRequired);
    const noValueSizeValidator = new Size({});
    assert.isNotTrue(noValueSizeValidator.impliesRequired);
    assert.isTrue(noValueSizeValidator.validate(''));
    assert.isTrue(noValueSizeValidator.validate('aaa'));
    const noArgSizeValidator = new Size();
    assert.isNotTrue(noArgSizeValidator.impliesRequired);
    assert.isTrue(noArgSizeValidator.validate(''));
    assert.isTrue(noArgSizeValidator.validate('aaa'));
  });

  it('Digits', () => {
    const validator = new Digits({ integer: 2, fraction: 3 });
    assert.isNotTrue(validator.impliesRequired);
    assert.isTrue(validator.validate('11.111'));
    assert.isTrue(validator.validate('1.1'));
    assert.isTrue(validator.validate('1'));
    assert.isFalse(validator.validate('1.1111'));
    assert.isFalse(validator.validate('111.111'));
    assert.isFalse(validator.validate('111.1111'));
  });

  it('Past', () => {
    const validator = new Past();
    assert.isNotTrue(validator.impliesRequired);
    assert.isTrue(validator.validate('2019-12-31'), 'past');
    assert.isFalse(validator.validate(String(new Date())), 'present');
    assert.isFalse(validator.validate('3000-01-01'), 'future');
  });

  // it("PastOrPresent", () => {
  //   const validator = new PastOrPresent();
  //   assert.isNotTrue(validator.impliesRequired);
  //   assert.isTrue(validator.validate("2019-12-31"), 'past');
  //   assert.isTrue(validator.validate(String(new Date())), 'present');
  //   assert.isFalse(validator.validate("3000-01-01"), 'future');
  // });

  it('Future', () => {
    const validator = new Future();
    assert.isNotTrue(validator.impliesRequired);
    assert.isFalse(validator.validate('2019-12-31'), 'past');
    assert.isFalse(validator.validate(String(new Date())), 'present');
    assert.isTrue(validator.validate('3000-01-01'), 'future');
  });

  // it("FutureOrPresent", () => {
  //   const validator = new FutureOrPresent();
  //   assert.isNotTrue(validator.impliesRequired);
  //   assert.isFalse(validator.validate("2019-12-31"), 'past');
  //   assert.isTrue(validator.validate(String(new Date())), 'present');
  //   assert.isTrue(validator.validate("3000-01-01"), 'future');
  // });

  it('Pattern', () => {
    let validator = new Pattern(/^(\+\d+)?([ -]?\d+){4,14}$/);
    assert.isNotTrue(validator.impliesRequired);
    assert.isFalse(validator.validate(''));
    assert.isFalse(validator.validate('123'));
    assert.isFalse(validator.validate('abcdefghijk'));
    assert.isTrue(validator.validate('+35 123 456 789'));
    assert.isTrue(validator.validate('123 456 789'));
    assert.isTrue(validator.validate('123-456-789'));
    validator = new Pattern('\\d+');
    assert.isTrue(validator.validate('1'));
    assert.isFalse(validator.validate('a'));
    validator = new Pattern({ regexp: '\\w+\\\\' });
    assert.isFalse(validator.validate('a'));
    assert.isTrue(validator.validate('a\\'));
    validator = new Pattern({ regexp: /\w+\\/ });
    assert.isFalse(validator.validate('a'));
    assert.isTrue(validator.validate('a\\'));
  });
});
