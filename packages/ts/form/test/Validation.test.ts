/* eslint-disable no-unused-expressions, no-shadow */
import { assert, expect, use } from '@esm-bundle/chai';
import { EndpointValidationError } from '@hilla/frontend';
import chaiDom from 'chai-dom';
import { css, html, LitElement } from 'lit';
import { customElement, query } from 'lit/decorators.js';
import { repeat } from 'lit/directives/repeat.js';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
// API to test
import {
  Binder,
  field,
  type InterpolateMessageCallback,
  NotBlank,
  Required,
  Size,
  ValidationError,
  type Validator,
  type ValueError,
} from '../src/index.js';
import {
  type Customer,
  type IdEntity,
  IdEntityModel,
  type Order,
  OrderModel,
  type TestEntity,
  TestModel,
  type TestMessageInterpolationEntity,
  TestMessageInterpolationModel,
} from './TestModels.js';

use(sinonChai);
use(chaiDom);

class NumberOutput extends HTMLElement {
  inputElement = document.createElement('input');

  get value(): string {
    return this.checkValidity() ? this.inputElement.value || '' : '';
  }

  set value(value: string) {
    this.inputElement.value = value;
  }

  checkValidity(): boolean {
    const numericValue = Number(this.inputElement.value);
    return !Number.isNaN(numericValue) && numericValue.toString() === this.inputElement.value;
  }
}
customElements.define('number-output', NumberOutput);

@customElement('order-view')
class OrderView extends LitElement {
  static override readonly styles = css`
    input[invalid] {
      border: 2px solid red;
    }
  `;

  binder = new Binder(this, OrderModel);

  @query('#submitting')
  submitting!: HTMLInputElement;

  @query('#notes')
  notes!: HTMLInputElement;

  @query('#fullName')
  fullName!: HTMLInputElement;

  @query('#nickName')
  nickName!: HTMLInputElement;

  @query('#add')
  add!: Element;

  @query('#description0')
  description!: HTMLInputElement;

  @query('#price0')
  price!: HTMLInputElement;

  @query('#priceError0')
  priceError!: HTMLOutputElement;

  @query('#total')
  total!: NumberOutput;

  override render() {
    const {
      notes,
      products,
      customer: { fullName, nickName },
      total,
    } = this.binder.model;

    return html`
      <input id="notes" ...="${field(notes)}" />
      <input id="fullName" ...="${field(fullName)}" />
      <input id="nickName" ...="${field(nickName)}" />
      <button id="add" @click=${() => this.binder.for(products).appendItem()}>+</button>
      ${repeat(
        products,
        ({ model: { description, price } }, index) => html`<div>
          <input id="description${index}" ...="${field(description)}" />
          <input id="price${index}" ...="${field(price)}" />
          <output id="priceError${index}">
            ${this.binder
              .for(price)
              .errors.map((error) => error.message)
              .join('\n')}
          </output>
        </div>`,
      )}
      <h4>Total: <number-output id="total" ${field(total)}></number-output></h4>
      <div id="submitting">${this.binder.submitting}</div>
    `;
  }
}

declare global {
  interface HTMLElementTagNameMap {
    'order-view': OrderView;
  }
}

const sleep = async (t: number) =>
  new Promise<void>((resolve) => {
    setTimeout(() => resolve(), t);
  });

const fireEvent = async (elm: Element, name: string) => {
  elm.dispatchEvent(new CustomEvent(name));
  return sleep(0);
};

describe('@hilla/form', () => {
  describe('Validation', () => {
    let binder: Binder<Order, OrderModel>;
    const view = document.createElement('div');

    beforeEach(async () => {
      binder = new Binder(view, OrderModel);
    });

    it('should run all validators per model', async () =>
      binder
        .for(binder.model.customer)
        .validate()
        .then((errors) => {
          // eslint-disable-next-line @typescript-eslint/require-array-sort-compare
          expect(errors.map((e) => e.validator.constructor.name).sort()).to.eql(['Required', 'Size']);
        }));

    it('should run all nested validations per model', async () =>
      binder.validate().then((errors) => {
        expect(errors.map((e) => e.property)).to.eql(['customer.fullName', 'customer.fullName', 'notes']);
      }));

    it('should run all validations per array items', async () => {
      binder.for(binder.model.products).appendItem();
      binder.for(binder.model.products).appendItem();
      return binder.validate().then((errors) => {
        expect(errors.map((e) => e.property)).to.eql([
          'customer.fullName',
          'customer.fullName',
          'notes',
          'products.0.description',
          'products.0.price',
          'products.1.description',
          'products.1.price',
        ]);
      });
    });

    describe('clearing', () => {
      ['reset', 'clear'].forEach((methodName) => {
        it(`should reset validation on ${methodName}`, async () => {
          await binder.validate();
          expect(binder.invalid).to.be.true;
          expect(binder.for(binder.model.customer.fullName).invalid).to.be.true;

          // eslint-disable-next-line @typescript-eslint/no-unsafe-call, @typescript-eslint/no-unsafe-member-access
          (binder as any)[methodName]();

          expect(binder.invalid).to.be.false;
          expect(binder.for(binder.model.customer.fullName).invalid).to.be.false;
        });
      });
    });

    describe('submitTo', () => {
      it('should be able to call submit() if onSubmit is pre configured', async () => {
        const testBinder = new Binder(view, TestModel, {
          onSubmit: async () => {
            // do nothing
          },
        });
        const binderSubmitToSpy = sinon.spy(testBinder, 'submitTo');
        await testBinder.submit();
        sinon.assert.calledOnce(binderSubmitToSpy);
      });

      it('should return the result of the endpoint call when calling submit()', async () => {
        const testBinder = new Binder(view, TestModel, { onSubmit: async (testEntity) => testEntity });
        const result = await testBinder.submit();
        assert.deepEqual(result, testBinder.value);
      });

      it('should throw on validation failure', async () => {
        try {
          await binder.submitTo(async () => {
            // do nothing
          });
          expect.fail();
        } catch (error: any) {
          // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
          expect(error.errors.length).to.gt(0);
        }
      });

      it('should re-throw on server failure', async () => {
        binder.for(binder.model.customer.fullName).value = 'foobar';
        binder.for(binder.model.notes).value = 'whatever';
        try {
          await binder.submitTo(async () => {
            throw new Error('whatever');
          });
          expect.fail();
        } catch (error: any) {
          // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
          expect(error.message).to.be.equal('whatever');
        }
      });

      it('should wrap server validation error', async () => {
        binder.for(binder.model.customer.fullName).value = 'foobar';
        binder.for(binder.model.notes).value = 'whatever';
        try {
          await binder.submitTo(async () => {
            throw new EndpointValidationError("Validation error in endpoint 'MyEndpoint' method 'saveMyBean'", [
              {
                message:
                  "Object of type 'com.example.MyBean' has invalid property 'foo' with value 'baz', validation error: 'custom message'",
                parameterName: 'foo',
              },
            ]);
          });
          expect.fail();
        } catch (e: any) {
          expect(e).to.be.instanceof(ValidationError);
          const error = e as ValidationError;
          expect(error.errors[0].message).to.be.equal('custom message');
          expect(error.errors[0].value).to.be.equal('baz');
          expect(error.errors[0].property).to.be.equal('foo');
        }
      });

      it('should wrap server validation error with any message', async () => {
        binder.for(binder.model.customer.fullName).value = 'foobar';
        binder.for(binder.model.notes).value = 'whatever';
        try {
          await binder.submitTo(async () => {
            throw new EndpointValidationError("Validation error in endpoint 'MyEndpoint' method 'saveMyBean'", [
              {
                message: 'Custom server message',
                parameterName: 'bar',
              },
            ]);
          });
          expect.fail();
        } catch (e: any) {
          expect(e).to.be.instanceof(ValidationError);
          const error = e as ValidationError;
          expect(error.errors[0].message).to.be.equal('Custom server message');
          expect(error.errors[0].value).to.be.undefined;
          expect(error.errors[0].property).to.be.equal('bar');
        }
      });

      it('record level cross field validation', async () => {
        const byPropertyName = (value: string) => (error: ValueError<any>) => {
          const propertyName = typeof error.property === 'string' ? error.property : binder.for(error.property).name;
          return propertyName === value;
        };

        const recordValidator = {
          message: 'cannot be the same',
          validate(value: Order) {
            if (value.customer.fullName === value.customer.nickName) {
              return { property: binder.model.customer.nickName };
            }

            return true;
          },
        };
        binder.addValidator(recordValidator);

        binder.for(binder.model.customer.fullName).value = 'foo';
        await binder.validate().then((errors) => {
          const crossFieldError = errors.find((error) => error.validator === recordValidator);
          expect(crossFieldError, 'recordValidator should not cause an error').to.be.undefined;
        });

        binder.for(binder.model.customer.nickName).value = 'foo';
        return binder.validate().then((errors) => {
          const crossFieldError = errors.find(byPropertyName('customer.nickName'));
          expect(crossFieldError).not.to.be.undefined;
          expect(crossFieldError?.message).to.equal('cannot be the same');
        });
      });

      it('record level cross field validation when the property is a string', async () => {
        const byPropertyName = (value: string) => (error: ValueError<any>) => {
          const propertyName = typeof error.property === 'string' ? error.property : binder.for(error.property).name;
          return propertyName === value;
        };

        const recordValidator = {
          message: 'cannot be the same',
          validate(value: Order) {
            if (value.customer.fullName === value.customer.nickName) {
              return { property: 'customer.nickName' };
            }

            return true;
          },
        };
        binder.addValidator(recordValidator);

        binder.for(binder.model.customer.fullName).value = 'foo';
        await binder.validate().then((errors) => {
          const crossFieldError = errors.find((error) => error.validator === recordValidator);
          expect(crossFieldError, 'recordValidator should not cause an error').to.be.undefined;
        });

        binder.for(binder.model.customer.nickName).value = 'foo';
        await binder.validate().then((errors) => {
          const crossFieldError = errors.find(byPropertyName('customer.nickName'));
          expect(crossFieldError).not.to.be.undefined;
          expect(crossFieldError?.message).to.equal('cannot be the same');
        });

        const customerValidator = {
          message: 'cannot be anagram of full name',
          validate(value: Customer) {
            if (Array.from(value.fullName).reverse().join('') === value.nickName) {
              return { property: 'nickName' };
            }

            return true;
          },
        };
        binder.for(binder.model.customer).addValidator(customerValidator);

        binder.for(binder.model.customer.nickName).value = 'oof';
        await binder.validate().then((errors) => {
          const crossFieldError = errors.find(byPropertyName('customer.nickName'));
          expect(crossFieldError).not.to.be.undefined;
          expect(crossFieldError?.message).to.equal('cannot be anagram of full name');
        });
      });
    });

    describe('model add validator', () => {
      let idBinder: Binder<IdEntity, IdEntityModel>;

      beforeEach(async () => {
        idBinder = new Binder(view, IdEntityModel);
      });

      it('should not have validation errors for a model without validators', async () => {
        assert.isEmpty(await idBinder.validate());
      });

      it('should not have validation errors for a validator that returns true', async () => {
        idBinder.addValidator({ message: 'foo', validate: () => true });
        assert.isEmpty(await idBinder.validate());
      });

      it('should not have validation errors for a validator that returns an empty array', async () => {
        idBinder.addValidator({ message: 'foo', validate: () => [] });
        assert.isEmpty(await idBinder.validate());
      });

      it('should fail validation after adding a synchronous validator to the model', async () => {
        idBinder.addValidator({ message: 'foo', validate: () => false });
        return idBinder.validate().then((errors) => {
          expect(errors[0].message).to.equal('foo');
          expect(errors[0].property).to.equal('');
          expect(errors[0].value).to.eql({ idString: '' });
        });
      });

      it('should fail validation after adding an asynchronous validator to the model', async () => {
        class AsyncValidator implements Validator<Order> {
          message = 'bar';

          async validate() {
            await sleep(10);
            return false;
          }
        }

        idBinder.addValidator(new AsyncValidator());
        return idBinder.validate().then((errors) => {
          expect(errors[0].message).to.equal('bar');
        });
      });

      it('should not have validations errors after adding validators to properties if property is not required', async () => {
        idBinder.for(idBinder.model.idString).addValidator({ message: 'foo', validate: () => false });
        const errors = await idBinder.validate();
        assert.isEmpty(errors);
      });

      it('should fail after adding validators to properties if property is not required but it has a value', async () => {
        idBinder.for(idBinder.model.idString).value = 'bar';
        idBinder.for(idBinder.model.idString).addValidator({ message: 'foo', validate: () => false });
        const errors = await idBinder.validate();
        expect(errors[0].message).to.equal('foo');
        expect(errors[0].property).to.equal('idString');
        expect(errors[0].value).to.eql('bar');
      });

      it('should fail after adding validators to properties if required and not value', async () => {
        idBinder.for(idBinder.model.idString).addValidator({ message: 'foo', validate: () => false });
        idBinder.for(idBinder.model.idString).addValidator(new Required());
        const errors = await idBinder.validate();
        expect(errors.length).to.equal(2);
      });

      it('should fail when validator returns a single ValidationResult', async () => {
        idBinder.addValidator({ message: 'foo', validate: () => ({ property: idBinder.model.idString }) });
        return idBinder.validate().then((errors) => {
          expect(errors[0].message).to.equal('foo');
          expect(errors[0].property).to.equal(idBinder.model.idString);
          expect(errors[0].value).to.eql({ idString: '' });
        });
      });

      it('should fail when validator returns an array of ValidationResult objects', async () => {
        idBinder.addValidator({ message: 'foo', validate: () => [{ property: idBinder.model.idString }] });
        return idBinder.validate().then((errors) => {
          expect(errors[0].message).to.equal('foo');
          expect(errors[0].property).to.equal(idBinder.model.idString);
          expect(errors[0].value).to.eql({ idString: '' });
        });
      });

      it('should not cause required by default', async () => {
        idBinder.for(idBinder.model.idString).addValidator({
          message: 'foo',
          validate: () => false,
        });
        expect(idBinder.for(idBinder.model.idString).required).to.be.false;
      });

      it('should cause required when having impliesRequired: true', async () => {
        idBinder.for(idBinder.model.idString).addValidator({
          message: 'foo',
          validate: () => false,
        });
        idBinder.for(idBinder.model.idString).addValidator({
          impliesRequired: true,
          message: 'foo',
          validate: () => false,
        });
        expect(idBinder.for(idBinder.model.idString).required).to.be.true;
      });
    });

    describe('model add validator (multiple fields)', () => {
      let testBinder: Binder<TestEntity, TestModel>;

      beforeEach(async () => {
        testBinder = new Binder(view, TestModel);
      });

      it('should fail when validator returns an array of ValidationResult objects', async () => {
        testBinder.addValidator({
          message: 'foo',
          validate: () => [
            { property: testBinder.model.fieldString },
            { property: testBinder.model.fieldNumber },
            // eslint-disable-next-line sort-keys
            { property: testBinder.model.fieldBoolean, message: 'bar' },
          ],
        });
        return testBinder.validate().then((errors) => {
          expect(errors).has.lengthOf(3);
          expect(errors[0].message).to.equal('foo');
          expect(errors[0].value).to.eql(TestModel.createEmptyValue());

          expect(errors[0].property).to.equal(testBinder.model.fieldString);
          expect(errors[1].property).to.equal(testBinder.model.fieldNumber);
          expect(errors[2].property).to.equal(testBinder.model.fieldBoolean);
          expect(errors[2].message).to.equal('bar');
        });
      });
    });

    describe('field element', () => {
      let orderView: OrderView;

      beforeEach(async () => {
        orderView = new OrderView();
        // eslint-disable-next-line prefer-destructuring
        binder = orderView.binder;
        document.body.append(orderView);
        await orderView.updateComplete;
      });

      afterEach(() => {
        orderView.remove();
      });

      const EVENTS = ['change', 'blur'];

      describe('should validate field on events', () => {
        EVENTS.forEach((event) => {
          it(event, async () => {
            expect(orderView.notes).to.not.have.attribute('invalid');
            await fireEvent(orderView.notes, event);
            expect(orderView.notes).to.have.attribute('invalid');
          });
        });
      });

      describe('should validate field of nested model on events', () => {
        EVENTS.forEach((event) => {
          it(event, async () => {
            await fireEvent(orderView.add, 'click');
            expect(orderView.description).to.not.have.attribute('invalid');
            await fireEvent(orderView.description, event);
            expect(orderView.description).to.have.attribute('invalid');
          });
        });
      });

      it(`should not validate field on input when first visit`, async () => {
        expect(orderView.notes).to.not.have.attribute('invalid');
        await fireEvent(orderView.notes, 'input');
        expect(orderView.notes).to.not.have.attribute('invalid');
      });

      it(`should validate field on input after first visit`, async () => {
        orderView.notes.value = 'foo';
        await fireEvent(orderView.notes, 'blur');
        expect(orderView.notes).to.not.have.attribute('invalid');

        orderView.notes.value = '';
        await fireEvent(orderView.notes, 'input');
        expect(orderView.notes).to.have.attribute('invalid');
      });

      it(`should validate fields on submit`, async () => {
        expect(orderView.notes).to.not.have.attribute('invalid');
        expect(orderView.fullName).to.not.have.attribute('invalid');
        expect(orderView.nickName).to.not.have.attribute('invalid');

        try {
          await orderView.binder.submitTo(async (item) => item);
          expect.fail();
        } catch (error) {
          // do nothing
        }

        expect(orderView.notes).to.have.attribute('invalid');
        expect(orderView.fullName).to.have.attribute('invalid');
        expect(orderView.nickName).to.not.have.attribute('invalid');
      });

      it(`should validate fields of nested model on submit`, async () => {
        expect(orderView.description).to.be.null;
        await fireEvent(orderView.add, 'click');
        await fireEvent(orderView.add, 'click');

        expect(orderView.description).to.not.have.attribute('invalid');
        expect(orderView.price).to.not.have.attribute('invalid');

        try {
          await orderView.binder.submitTo(async (item) => item);
          expect.fail();
        } catch (error) {
          expect((error as ValidationError).errors.map((e) => e.property)).to.be.eql([
            'customer.fullName',
            'customer.fullName',
            'notes',
            'products.0.description',
            'products.0.price',
            'products.1.description',
            'products.1.price',
          ]);
        }

        expect(orderView.description).to.have.attribute('invalid');
        expect(orderView.price).to.have.attribute('invalid');
        expect(String(orderView.priceError.textContent).trim()).to.equal('must be greater than 0');
      });

      it(`should validate fields of arrays on submit`, async () => {
        expect(orderView.description).to.be.null;
        await fireEvent(orderView.add, 'click');
        await fireEvent(orderView.add, 'click');

        expect(orderView.description).to.not.have.attribute('invalid');
        expect(orderView.price).to.not.have.attribute('invalid');

        try {
          await orderView.binder.submitTo(async (item) => item);
          expect.fail();
        } catch (error) {
          // do nothing
        }

        expect(orderView.description).to.have.attribute('invalid');
        expect(orderView.price).to.have.attribute('invalid');
      });

      it(`should not submit when just validation fails`, async () => {
        expect(orderView.description).to.be.null;
        await fireEvent(orderView.add, 'click');

        orderView.notes.value = 'foo';
        await fireEvent(orderView.notes, 'change');
        orderView.fullName.value = 'manuel';
        await fireEvent(orderView.fullName, 'change');
        orderView.description.value = 'bread';
        await fireEvent(orderView.description, 'change');

        try {
          await orderView.binder.submitTo(async (item) => item);
          expect.fail();
        } catch (error) {
          // do nothing
        }
      });

      it(`should submit when no validation errors`, async () => {
        expect(orderView.description).to.be.null;
        await fireEvent(orderView.add, 'click');

        orderView.notes.value = 'foo';
        await fireEvent(orderView.notes, 'change');
        orderView.fullName.value = 'manuel';
        await fireEvent(orderView.fullName, 'change');
        orderView.description.value = 'bread';
        await fireEvent(orderView.description, 'change');
        orderView.price.value = '10';
        await fireEvent(orderView.price, 'change');

        const item = await orderView.binder.submitTo(async (order) => order);
        expect(item).not.to.be.undefined;
        expect(item.products[0].description).to.be.equal('bread');
        expect(item.products[0].price).to.be.equal(10);
        expect(item.notes).to.be.equal('foo');
        expect(item.customer.fullName).to.be.equal('manuel');
      });

      it('should display server validation error', async () => {
        binder.for(binder.model.customer.fullName).value = 'foobar';
        binder.for(binder.model.notes).value = 'whatever';
        const requestUpdateSpy = sinon.spy(orderView, 'requestUpdate');
        try {
          await binder.submitTo(async () => {
            requestUpdateSpy.resetHistory();
            throw new EndpointValidationError('Validation error in endpoint "MyEndpoint" method "saveMyBean"', [
              {
                message: 'Invalid notes',
                parameterName: 'notes',
              },
            ]);
          });
          expect.fail();
        } catch (error) {
          sinon.assert.calledOnce(requestUpdateSpy);
          await orderView.updateComplete;
          expect(binder.for(binder.model.notes).invalid).to.be.true;
          expect(binder.for(binder.model.notes).ownErrors[0].message).to.equal('Invalid notes');
        }
      });

      it('should display submitting state during submission', async () => {
        binder.for(binder.model.customer.fullName).value = 'Jane Doe';
        binder.for(binder.model.notes).value = 'foo';
        await orderView.updateComplete;
        expect(binder.submitting).to.be.false;
        const requestUpdateSpy = sinon.spy(orderView, 'requestUpdate');

        const endpoint = sinon.stub().callsFake(async () => {
          sinon.assert.called(requestUpdateSpy);
          expect(binder.submitting).to.be.true;
          await orderView.updateComplete;
          expect(orderView.submitting.textContent).to.equal('true');
          requestUpdateSpy.resetHistory();
        });
        await binder.submitTo(endpoint);

        sinon.assert.called(endpoint);
        sinon.assert.called(requestUpdateSpy);
        expect(binder.submitting).to.be.false;
        await orderView.updateComplete;
        expect(orderView.submitting.textContent).to.equal('false');
      });

      // https://github.com/vaadin/flow/issues/8688
      it('should update binder properties after submit when a field changes value', async () => {
        try {
          await orderView.binder.submitTo(async (item) => item);
          expect.fail();
        } catch (error) {
          // do nothing
        }
        const errorsOnSubmit = binder.errors.length;

        orderView.notes.value = 'foo';
        await fireEvent(orderView.notes, 'change');
        const numberOfValidatorsOnNotesField = binder.for(binder.model.notes).validators.length;

        if (errorsOnSubmit >= 1) {
          assert.equal(errorsOnSubmit - numberOfValidatorsOnNotesField, binder.errors.length);
        }
      });

      it('should display error for NaN in number field', async () => {
        await fireEvent(orderView.add, 'click');

        orderView.price.value = 'not a number';
        await fireEvent(orderView.price, 'change');

        expect(String(orderView.priceError.textContent)).to.contain('must be a number');
      });

      it('should fail validation when element.checkValidity() is false', async () => {
        const value = binder.defaultValue;
        value.customer.fullName = 'Jane Doe';
        value.notes = '42';
        value.total = 1;
        binder.value = value;
        await orderView.updateComplete;

        // Verify initially valid state
        let errors = await binder.validate();
        expect(errors).to.have.length(0);

        // Simulate good user input
        orderView.total.value = '2';
        await fireEvent(orderView.total, 'change');

        errors = await binder.validate();
        expect(errors).to.have.length(0);

        // Simulate built-in validation error
        orderView.total.value = 'not a number';
        await fireEvent(orderView.total, 'change');

        errors = await binder.validate();
        expect(errors).to.have.length(1);
        expect(errors[0]).to.have.property('property', 'total');

        // Simulate bad user input
        orderView.total.value = '';
        orderView.total.inputElement.value = 'not a number';
        await fireEvent(orderView.total, 'change');

        errors = await binder.validate();
        expect(errors).to.have.length(1);
        expect(errors[0]).to.have.property('property', 'total');

        // Correction
        orderView.total.value = '2';
        await fireEvent(orderView.total, 'change');

        errors = await binder.validate();
        expect(errors).to.have.length(0);
      });
    });

    describe('message interpolation', () => {
      let testMessageBinder: Binder<TestMessageInterpolationEntity, TestMessageInterpolationModel>;

      beforeEach(async () => {
        testMessageBinder = new Binder(view, TestMessageInterpolationModel);
      });

      afterEach(async () => {
        Binder.interpolateMessageCallback = undefined;
      });

      it('should interpolate message', async () => {
        const callback: InterpolateMessageCallback<any> = (_message, _validator, _binderNode) =>
          // Interpolates all error messages as 'custom message'
          'custom message';
        Binder.interpolateMessageCallback = sinon.spy(callback);

        const errors = await testMessageBinder.validate();
        expect(Binder.interpolateMessageCallback).to.be.called;
        expect(errors).to.have.lengthOf.at.least(1);
        for (const [i, error] of errors.entries()) {
          expect(error.message).to.equal('custom message', `errors[${i}]`);
        }
      });

      it('should have access to message and validator', async () => {
        const callback: InterpolateMessageCallback<any> = (message, validator, _binderNode) => {
          expect(message).to.be.a('string');
          expect(validator).to.have.property('validate').that.is.a('function');
          if (validator instanceof NotBlank) {
            return message.replace('not be', '*NOT BE*');
          }
          return message;
        };
        Binder.interpolateMessageCallback = sinon.spy(callback);

        const errors = await testMessageBinder.validate();
        expect(Binder.interpolateMessageCallback).to.be.called;
        const error = errors.find((e) => e.validator instanceof NotBlank);
        expect(error?.message).to.equal('must *NOT BE* blank');
      });

      it('should have access to BinderNode', async () => {
        testMessageBinder.read({ stringMinSize: '123', stringNotBlank: 'not blank' });
        const callback: InterpolateMessageCallback<any> = (message, _validator, binderNode) => {
          expect(binderNode).to.have.property('model');
          return `[value: ${String(binderNode.value)}] ${message}`;
        };
        Binder.interpolateMessageCallback = sinon.spy(callback);

        const errors = await testMessageBinder.validate();
        expect(Binder.interpolateMessageCallback).to.be.called;
        const error = errors.find((e) => e.validator instanceof Size);
        expect(error?.message ?? '').to.include('[value: 123]');
      });
    });
  });
});
