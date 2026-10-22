// Kept out of `Validation.test.ts`, which is mirrored verbatim in
// `test/old/Validation.test.ts` for the legacy model API.
import { assert, describe, expect, it } from 'vitest';
// API to test
import { Binder, NotEmpty, Size } from '../src/index.js';
import { TestModel } from './TestModels.js';

describe('@vaadin/hilla-lit-form', () => {
  describe('Validation', () => {
    describe('empty values', () => {
      const view = document.createElement('div');

      it('should report the violated constraint when a required field is undefined', async () => {
        const binder = new Binder(view, TestModel);
        const optionalString = binder.for(binder.model.fieldOptionalString);
        // `NotEmpty` makes the node required, so every validator of the node is
        // run even though the value is empty.
        optionalString.addValidator(new NotEmpty());
        optionalString.addValidator(new Size({ max: 255 }));
        assert.isUndefined(optionalString.value);

        const errors = await optionalString.validate();
        expect(errors.map((e) => e.validator.constructor.name)).to.eql(['NotEmpty']);
      });
    });
  });
});
