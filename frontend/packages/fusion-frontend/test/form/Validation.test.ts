/* eslint-disable lit/no-template-arrow, no-unused-expressions, no-shadow */
import { assert, expect } from '@open-wc/testing';
import sinon from 'sinon';
import { css, html, LitElement } from 'lit';
import { repeat } from 'lit/directives/repeat.js';
import { customElement, query } from 'lit/decorators.js';
// API to test
import { Binder, field, Required, ValidationError, Validator, ValueError } from '../../src';
import { IdEntity, IdEntityModel, Order, OrderModel, TestEntity, TestModel } from './TestModels';

@customElement('order-view')
class OrderView extends LitElement {
  binder = new Binder(this, OrderModel);

  @query('#submitting') submitting!: HTMLInputElement;

  @query('#notes') notes!: HTMLInputElement;

  @query('#fullName') fullName!: HTMLInputElement;

  @query('#nickName') nickName!: HTMLInputElement;

  @query('#add') add!: Element;

  @query('#description0') description!: HTMLInputElement;

  @query('#price0') price!: HTMLInputElement;

  @query('#priceError0') priceError!: HTMLOutputElement;

  static get styles() {
    return css`
      input[invalid] {
        border: 2px solid red;
      }
    `;
  }

  render() {
    const {
      notes,
      products,
      customer: { fullName, nickName },
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
        </div>`
      )}
      <div id="submitting">${this.binder.submitting}</div>
    `;
  }
}

const sleep = async (t: number) => new Promise<void>((resolve) => setTimeout(() => resolve(), t));
const fireEvent = async (elm: Element, name: string) => {
  elm.dispatchEvent(new CustomEvent(name));
  return sleep(0);
};

describe('form/Validation', () => {
  let binder: Binder<Order, OrderModel<Order>>;
  const view = document.createElement('div');

  beforeEach(async () => {
    binder = new Binder(view, OrderModel);
  });

  it('should run all validators per model', async () => {
    return binder
      .for(binder.model.customer)
      .validate()
      .then((errors) => {
        expect(errors.map((e) => e.validator.constructor.name).sort()).to.eql(['Required', 'Size']);
      });
  });

  it('should run all nested validations per model', async () => {
    return binder.validate().then((errors) => {
      expect(errors.map((e) => e.property)).to.eql(['customer.fullName', 'customer.fullName', 'notes']);
    });
  });

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

        (binder as any)[methodName]();

        expect(binder.invalid).to.be.false;
        expect(binder.for(binder.model.customer.fullName).invalid).to.be.false;
      });
    });
  });

  describe('submitTo', () => {
    it('should be able to call submit() if onSubmit is pre configured', async () => {
      const binder = new Binder(view, TestModel, {
        onSubmit: async () => {
          // do nothing
        },
      });
      const binderSubmitToSpy = sinon.spy(binder, 'submitTo');
      await binder.submit();
      sinon.assert.calledOnce(binderSubmitToSpy);
    });

    it('should return the result of the endpoint call when calling submit()', async () => {
      const binder = new Binder(view, TestModel, { onSubmit: async (testEntity) => testEntity });
      const result = await binder.submit();
      assert.deepEqual(result, binder.value);
    });

    it('should throw on validation failure', async () => {
      try {
        await binder.submitTo(async () => {
          // do nothing
        });
        expect.fail();
      } catch (error) {
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
      } catch (error) {
        expect(error.message).to.be.equal('whatever');
      }
    });

    it('should wrap server validation error', async () => {
      binder.for(binder.model.customer.fullName).value = 'foobar';
      binder.for(binder.model.notes).value = 'whatever';
      try {
        await binder.submitTo(async () => {
          // eslint-disable-next-line no-throw-literal
          throw {
            message: "Validation error in endpoint 'MyEndpoint' method 'saveMyBean'",
            validationErrorData: [
              {
                message:
                  "Object of type 'com.example.MyBean' has invalid property 'foo' with value 'baz', validation error: 'custom message'",
                parameterName: 'foo',
              },
            ],
          };
        });
        expect.fail();
      } catch (error) {
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
          // eslint-disable-next-line no-throw-literal
          throw {
            message: "Validation error in endpoint 'MyEndpoint' method 'saveMyBean'",
            validationErrorData: [
              {
                message: 'Custom server message',
                parameterName: 'bar',
              },
            ],
          };
        });
        expect.fail();
      } catch (error) {
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
        validate(value: Order) {
          if (value.customer.fullName === value.customer.nickName) {
            return { property: binder.model.customer.nickName };
          }

          return true;
        },
        message: 'cannot be the same',
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
  });

  describe('model add validator', () => {
    let binder: Binder<IdEntity, IdEntityModel<IdEntity>>;

    beforeEach(async () => {
      binder = new Binder(view, IdEntityModel);
    });

    it('should not have validation errors for a model without validators', async () => {
      assert.isEmpty(await binder.validate());
    });

    it('should not have validation errors for a validator that returns true', async () => {
      binder.addValidator({ message: 'foo', validate: () => true });
      assert.isEmpty(await binder.validate());
    });

    it('should not have validation errors for a validator that returns an empty array', async () => {
      binder.addValidator({ message: 'foo', validate: () => [] });
      assert.isEmpty(await binder.validate());
    });

    it('should fail validation after adding a synchronous validator to the model', async () => {
      binder.addValidator({ message: 'foo', validate: () => false });
      return binder.validate().then((errors) => {
        expect(errors[0].message).to.equal('foo');
        expect(errors[0].property).to.equal('');
        expect(errors[0].value).to.eql({ idString: '' });
      });
    });

    it('should fail validation after adding an asynchronous validator to the model', async () => {
      class AsyncValidator implements Validator<Order> {
        message = 'bar';

        validate = async () => {
          await sleep(10);
          return false;
        };
      }
      binder.addValidator(new AsyncValidator());
      return binder.validate().then((errors) => {
        expect(errors[0].message).to.equal('bar');
      });
    });

    it('should not have validations errors after adding validators to properties if property is not required', async () => {
      binder.for(binder.model.idString).addValidator({ message: 'foo', validate: () => false });
      const errors = await binder.validate();
      assert.isEmpty(errors);
    });

    it('should fail after adding validators to properties if property is not required but it has a value', async () => {
      binder.for(binder.model.idString).value = 'bar';
      binder.for(binder.model.idString).addValidator({ message: 'foo', validate: () => false });
      const errors = await binder.validate();
      expect(errors[0].message).to.equal('foo');
      expect(errors[0].property).to.equal('idString');
      expect(errors[0].value).to.eql('bar');
    });

    it('should fail after adding validators to properties if required and not value', async () => {
      binder.for(binder.model.idString).addValidator({ message: 'foo', validate: () => false });
      binder.for(binder.model.idString).addValidator(new Required());
      const errors = await binder.validate();
      expect(errors.length).to.equal(2);
    });

    it('should fail when validator returns a single ValidationResult', async () => {
      binder.addValidator({ message: 'foo', validate: () => ({ property: binder.model.idString }) });
      return binder.validate().then((errors) => {
        expect(errors[0].message).to.equal('foo');
        expect(errors[0].property).to.equal(binder.model.idString);
        expect(errors[0].value).to.eql({ idString: '' });
      });
    });

    it('should fail when validator returns an array of ValidationResult objects', async () => {
      binder.addValidator({ message: 'foo', validate: () => [{ property: binder.model.idString }] });
      return binder.validate().then((errors) => {
        expect(errors[0].message).to.equal('foo');
        expect(errors[0].property).to.equal(binder.model.idString);
        expect(errors[0].value).to.eql({ idString: '' });
      });
    });

    it('should not cause required by default', async () => {
      binder.for(binder.model.idString).addValidator({
        message: 'foo',
        validate: () => false,
      });
      expect(binder.for(binder.model.idString).required).to.be.false;
    });

    it('should cause required when having impliesRequired: true', async () => {
      binder.for(binder.model.idString).addValidator({
        message: 'foo',
        validate: () => false,
      });
      binder.for(binder.model.idString).addValidator({
        message: 'foo',
        validate: () => false,
        impliesRequired: true,
      });
      expect(binder.for(binder.model.idString).required).to.be.true;
    });
  });

  describe('model add validator (multiple fields)', () => {
    let binder: Binder<TestEntity, TestModel<TestEntity>>;

    beforeEach(async () => {
      binder = new Binder(view, TestModel);
    });

    it('should fail when validator returns an array of ValidationResult objects', async () => {
      binder.addValidator({
        message: 'foo',
        validate: () => [
          { property: binder.model.fieldString },
          { property: binder.model.fieldNumber },
          { property: binder.model.fieldBoolean, message: 'bar' },
        ],
      });
      return binder.validate().then((errors) => {
        expect(errors).has.lengthOf(3);
        expect(errors[0].message).to.equal('foo');
        expect(errors[0].value).to.eql(TestModel.createEmptyValue());

        expect(errors[0].property).to.equal(binder.model.fieldString);
        expect(errors[1].property).to.equal(binder.model.fieldNumber);
        expect(errors[2].property).to.equal(binder.model.fieldBoolean);
        expect(errors[2].message).to.equal('bar');
      });
    });
  });

  describe('field element', () => {
    let orderView: OrderView;

    beforeEach(async () => {
      orderView = document.createElement('order-view') as OrderView;
      binder = orderView.binder;
      document.body.appendChild(orderView);
      await orderView.updateComplete;
    });

    afterEach(async () => {
      document.body.removeChild(orderView);
    });

    ['change', 'blur'].forEach((event) => {
      it(`should validate field on ${event}`, async () => {
        expect(orderView.notes.hasAttribute('invalid')).to.be.false;
        await fireEvent(orderView.notes, event);
        expect(orderView.notes.hasAttribute('invalid')).to.be.true;
      });

      it(`should validate field of nested model on  ${event}`, async () => {
        await fireEvent(orderView.add, 'click');
        expect(orderView.description.hasAttribute('invalid')).to.be.false;
        await fireEvent(orderView.description, event);
        expect(orderView.description.hasAttribute('invalid')).to.be.true;
      });
    });

    it(`should not validate field on input when first visit`, async () => {
      expect(orderView.notes.hasAttribute('invalid')).to.be.false;
      await fireEvent(orderView.notes, 'input');
      expect(orderView.notes.hasAttribute('invalid')).to.be.false;
    });

    it(`should validate field on input after first visit`, async () => {
      orderView.notes.value = 'foo';
      await fireEvent(orderView.notes, 'blur');
      expect(orderView.notes.hasAttribute('invalid')).to.be.false;

      orderView.notes.value = '';
      await fireEvent(orderView.notes, 'input');
      expect(orderView.notes.hasAttribute('invalid')).to.be.true;
    });

    it(`should validate fields on submit`, async () => {
      expect(orderView.notes.hasAttribute('invalid')).to.be.false;
      expect(orderView.fullName.hasAttribute('invalid')).to.be.false;
      expect(orderView.nickName.hasAttribute('invalid')).to.be.false;

      try {
        await orderView.binder.submitTo(async (item) => item);
        expect.fail();
      } catch (error) {
        // do nothing
      }

      expect(orderView.notes.hasAttribute('invalid')).to.be.true;
      expect(orderView.fullName.hasAttribute('invalid')).to.be.true;
      expect(orderView.nickName.hasAttribute('invalid')).to.be.false;
    });

    it(`should validate fields of nested model on submit`, async () => {
      expect(orderView.description).to.be.null;
      await fireEvent(orderView.add, 'click');
      await fireEvent(orderView.add, 'click');

      expect(orderView.description.hasAttribute('invalid')).to.be.false;
      expect(orderView.price.hasAttribute('invalid')).to.be.false;

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

      expect(orderView.description.hasAttribute('invalid')).to.be.true;
      expect(orderView.price.hasAttribute('invalid')).to.be.true;
      expect(String(orderView.priceError.textContent).trim()).to.equal('must be greater than 0');
    });

    it(`should validate fields of arrays on submit`, async () => {
      expect(orderView.description).to.be.null;
      await fireEvent(orderView.add, 'click');
      await fireEvent(orderView.add, 'click');

      expect(orderView.description.hasAttribute('invalid')).to.be.false;
      expect(orderView.price.hasAttribute('invalid')).to.be.false;

      try {
        await orderView.binder.submitTo(async (item) => item);
        expect.fail();
      } catch (error) {
        // do nothing
      }

      expect(orderView.description.hasAttribute('invalid')).to.be.true;
      expect(orderView.price.hasAttribute('invalid')).to.be.true;
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

      const item = await orderView.binder.submitTo(async (item) => item);
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
          // eslint-disable-next-line no-throw-literal
          throw {
            message: 'Validation error in endpoint "MyEndpoint" method "saveMyBean"',
            validationErrorData: [
              {
                message: 'Invalid notes',
                parameterName: 'notes',
              },
            ],
          };
        });
        expect.fail();
      } catch (error) {
        sinon.assert.calledOnce(requestUpdateSpy);
        await orderView.updateComplete;
        expect(binder.for(binder.model.notes).invalid).to.be.true;
        expect(binder.for(binder.model.notes).ownErrors![0].message).to.equal('Invalid notes');
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
  });
});
