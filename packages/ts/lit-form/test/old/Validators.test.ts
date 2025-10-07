import { assert, describe, it } from 'vitest';
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
  ValidityStateValidator,
} from '../../src/index.js';

describe('@vaadin/hilla-lit-form', () => {
  describe('Validators', () => {
    it('custom error message', () => {
      assert.equal(
        // eslint-disable-next-line sort-keys
        new Size({ min: 1, max: 2 }).message,
        'size must be between 1 and 2',
        'when no custom error message is provided, the default error message should be used',
      );
      assert.equal(
        // eslint-disable-next-line sort-keys
        new Size({ min: 1, max: 2, message: 'Please enter something with the size between 1 and 2' }).message,
        'Please enter something with the size between 1 and 2',
        'when a custom error message is provided, it should be used instead of the default one',
      );
    });

    it('Required', () => {
      const validator = new Required();
      assert.isTrue(validator.impliesRequired);
      assert.equal(validator.name, 'Required');
      assert.isTrue(validator.validate('foo'));
      assert.isFalse(validator.validate(''));
      assert.isFalse(validator.validate(undefined));
      assert.isTrue(validator.validate(0));
    });

    it('Email', () => {
      const validator = new Email();
      assert.isNotTrue(validator.impliesRequired);
      assert.equal(validator.name, 'Email');
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
      assert.equal(validator.name, 'Null');
      assert.isTrue(validator.validate(null));
      assert.isTrue(validator.validate(undefined));
      assert.isFalse(validator.validate(''));
    });

    it('NotNull', () => {
      const validator = new NotNull();
      assert.isTrue(validator.impliesRequired);
      assert.equal(validator.name, 'NotNull');
      assert.isTrue(validator.validate(''));
      assert.isFalse(validator.validate(null));
      assert.isFalse(validator.validate(undefined));
    });

    it('NotEmpty', () => {
      const validator = new NotEmpty();
      assert.isTrue(validator.impliesRequired);
      assert.equal(validator.name, 'NotEmpty');
      assert.isTrue(validator.validate('a'));
      assert.isTrue(validator.validate(['a']));
      assert.isFalse(validator.validate(''));
      assert.isFalse(validator.validate(undefined));
      assert.isFalse(validator.validate([]));
    });

    it('NotBlank', () => {
      const validator = new NotBlank();
      assert.isTrue(validator.impliesRequired);
      assert.equal(validator.name, 'NotBlank');
      assert.isTrue(validator.validate('a'));
      assert.isFalse(validator.validate(''));
      assert.isFalse(validator.validate(undefined));
      assert.isFalse(validator.validate(' '));
      assert.isFalse(validator.validate('\t'));
    });

    it('AssertTrue', () => {
      const validator = new AssertTrue();
      assert.isNotTrue(validator.impliesRequired);
      assert.equal(validator.name, 'AssertTrue');
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
      assert.equal(validator.name, 'AssertFalse');
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
      assert.equal(validator.name, 'IsNumber');
      assert.isTrue(validator.validate(0));
      assert.isTrue(validator.validate(1));
      assert.isTrue(validator.validate(1.2));
      assert.isTrue(validator.validate(-0.5));
      assert.isTrue(validator.validate(6.757657e-8));
      assert.isTrue(validator.validate(6.757657e8));
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
      assert.equal(validator.name, 'Min');
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
      assert.equal(validator.name, 'Max');
      assert.isTrue(validator.validate(1));
      assert.isTrue(validator.validate(0.9));
      assert.isFalse(validator.validate(1.1));
    });

    it('DecimalMin', () => {
      let validator = new DecimalMin('30.1');
      assert.isNotTrue(validator.impliesRequired);
      assert.equal(validator.name, 'DecimalMin');
      assert.isFalse(validator.validate(1));
      assert.isTrue(validator.validate(30.1));
      assert.isTrue(validator.validate(30.2));
      assert.isTrue(validator.validate('30.2'));
      // eslint-disable-next-line sort-keys
      validator = new DecimalMin({ value: '30.1', inclusive: false });
      assert.isFalse(validator.validate(30.1));
    });

    it('DecimalMax', () => {
      let validator = new DecimalMax('30.1');
      assert.isNotTrue(validator.impliesRequired);
      assert.equal(validator.name, 'DecimalMax');
      assert.isTrue(validator.validate(30));
      assert.isTrue(validator.validate(30.1));
      assert.isFalse(validator.validate(30.2));
      // eslint-disable-next-line sort-keys
      validator = new DecimalMax({ value: '30.1', inclusive: false });
      assert.isFalse(validator.validate(30.1));
    });

    it('Negative', () => {
      const validator = new Negative();
      assert.isNotTrue(validator.impliesRequired);
      assert.equal(validator.name, 'Negative');
      assert.isTrue(validator.validate(-1));
      assert.isTrue(validator.validate(-0.01));
      assert.isFalse(validator.validate(0));
      assert.isFalse(validator.validate(1));
      assert.isTrue(validator.validate(undefined));
      assert.isTrue(validator.validate(null));
      assert.isTrue(validator.validate(''));
    });

    it('NegativeOrZero', () => {
      const validator = new NegativeOrZero();
      assert.isNotTrue(validator.impliesRequired);
      assert.equal(validator.name, 'NegativeOrZero');
      assert.isTrue(validator.validate(-1));
      assert.isTrue(validator.validate(-0.01));
      assert.isTrue(validator.validate(0));
      assert.isFalse(validator.validate(1));
      assert.isTrue(validator.validate(undefined));
      assert.isTrue(validator.validate(null));
      assert.isTrue(validator.validate(''));
    });

    it('Positive', () => {
      const validator = new Positive();
      assert.isNotTrue(validator.impliesRequired);
      assert.equal(validator.name, 'Positive');
      assert.isFalse(validator.validate(-1));
      assert.isFalse(validator.validate(-0.01));
      assert.isFalse(validator.validate(0));
      assert.isTrue(validator.validate(0.01));
      assert.isTrue(validator.validate(undefined));
      assert.isTrue(validator.validate(null));
      assert.isTrue(validator.validate(''));
    });

    it('PositiveOrZero', () => {
      const validator = new PositiveOrZero();
      assert.isNotTrue(validator.impliesRequired);
      assert.equal(validator.name, 'PositiveOrZero');
      assert.isFalse(validator.validate(-1));
      assert.isFalse(validator.validate(-0.01));
      assert.isTrue(validator.validate(0));
      assert.isTrue(validator.validate(0.01));
      assert.isTrue(validator.validate(undefined));
      assert.isTrue(validator.validate(null));
      assert.isTrue(validator.validate(''));
    });

    it('Size', () => {
      // eslint-disable-next-line sort-keys
      const validator = new Size({ min: 2, max: 4 });
      assert.isTrue(validator.impliesRequired);
      assert.equal(validator.name, 'Size');
      assert.isFalse(validator.validate(''));
      assert.isFalse(validator.validate('a'));
      assert.isTrue(validator.validate('aa'));
      assert.isTrue(validator.validate('aaa'));
      const noMinValidator = new Size({ max: 3 });
      assert.isNotTrue(noMinValidator.impliesRequired);
      // eslint-disable-next-line sort-keys
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
      // eslint-disable-next-line sort-keys
      const validator = new Digits({ integer: 2, fraction: 3 });
      assert.isNotTrue(validator.impliesRequired);
      assert.equal(validator.name, 'Digits');
      assert.isTrue(validator.validate('11.111'), 'Exact number of digits');
      assert.isTrue(validator.validate('1.1'), 'Less digits');
      assert.isTrue(validator.validate('1'), 'Less digits and no fraction');
      assert.isFalse(validator.validate('1.1111'), 'More fractional digits');
      assert.isFalse(validator.validate('111.111'), 'More integer digits');
      assert.isFalse(validator.validate('111'), 'More integer digits and no fraction');
      assert.isFalse(validator.validate('111.1111'), 'More integer and fractional digits');
      assert.isTrue(validator.validate('-11.111'), 'Exact number of digits, negative number');
      assert.isTrue(validator.validate('-1.1'), 'Less digits, negative number');
      assert.isTrue(validator.validate('-1'), 'Less digits and no fraction, negative number');
      assert.isFalse(validator.validate('-1.1111'), 'More fractional digits, negative number');
      assert.isFalse(validator.validate('-111.111'), 'More integer digits, negative number');
      assert.isFalse(validator.validate('-111'), 'More integer digits and no fraction, negative number');
      assert.isFalse(validator.validate('-111.1111'), 'More integer and fractional digits, negative number');
    });

    it('Past', () => {
      const validator = new Past();
      assert.isNotTrue(validator.impliesRequired);
      assert.equal(validator.name, 'Past');
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
      assert.equal(validator.name, 'Future');
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
      let validator = new Pattern(/^\+?\d(?:[ -]?\d){3,13}$/u);
      assert.isNotTrue(validator.impliesRequired);
      assert.equal(validator.name, 'Pattern');
      assert.isFalse(validator.validate(''));
      assert.isFalse(validator.validate('123'));
      assert.isFalse(validator.validate('abcdefghijk'));
      assert.isTrue(validator.validate('+35 123 456 789'));
      assert.isTrue(validator.validate('123 456 789'));
      assert.isTrue(validator.validate('123-456-789'));
      validator = new Pattern('\\d+');
      assert.isTrue(validator.validate('1'));
      assert.isFalse(validator.validate('a'));
      validator = new Pattern({ regexp: '\\w{1,10}\\\\' });
      assert.isFalse(validator.validate('a'));
      assert.isTrue(validator.validate('a\\'));
      validator = new Pattern({ regexp: /\w{1,10}\\/u });
      assert.isFalse(validator.validate('a'));
      assert.isTrue(validator.validate('a\\'));
      validator = new Pattern({ regexp: "^[\\p{L}\\s\\.,']+$" });
      assert.isFalse(validator.validate('123'));
      // https://www.kermitproject.org/utf8.html
      assert.isTrue(validator.validate("I can eat glass and it doesn't hurt me."));
      assert.isTrue(validator.validate('Я могу есть стекло, оно мне не вредит.'));
    });

    it('ValidityStateValidator', () => {
      const validator = new ValidityStateValidator();
      assert.isNotTrue(validator.impliesRequired);
      assert.equal(validator.name, 'ValidityStateValidator');
      assert.equal(validator.validate(), false);
      assert.equal(validator.message, '');
    });
  });
});
