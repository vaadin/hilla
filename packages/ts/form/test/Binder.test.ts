/* tslint:disable:max-classes-per-file */

import { assert, expect } from '@open-wc/testing';
import { LitElement } from 'lit';
import { customElement } from 'lit/decorators.js';
import sinon from 'sinon';
// API to test
import { Binder, BinderConfiguration } from '../src';
import { Employee, EmployeeModel, Order, OrderModel, TestEntity, TestModel } from './TestModels.js';

@customElement('lit-order-view')
class LitOrderView extends LitElement {}

@customElement('lit-employee-view')
class LitEmployeeView extends LitElement {}

describe('form/Binder', () => {
  const litOrderView = document.createElement('lit-order-view') as LitOrderView;
  const requestUpdateStub = sinon.stub(litOrderView, 'requestUpdate').resolves();

  afterEach(() => {
    requestUpdateStub.reset();
  });

  it('should instantiate without type arguments', () => {
    const binder = new Binder(litOrderView, OrderModel);

    assert.isDefined(binder);
    assert.isDefined(binder.value.notes);
    assert.isDefined(binder.value.idString);
    assert.isDefined(binder.value.customer.fullName);
    assert.isDefined(binder.value.customer.idString);
  });

  it('should instantiate model', () => {
    const binder = new Binder(litOrderView, OrderModel);

    assert.instanceOf(binder.model, OrderModel);
  });

  it('should be able to create a binder with a default onchange listener', () => {
    const binder = new Binder(litOrderView, OrderModel);

    binder.for(binder.model.notes).value = 'foo';

    expect(requestUpdateStub).to.be.calledTwice;
  });

  it('should be able to create a binder with a custom onchange listener', () => {
    let foo = 'bar';
    const config: BinderConfiguration<Order> = {
      onChange: () => {
        foo = 'baz';
      },
    };

    const binder = new Binder(litOrderView, OrderModel, config);

    binder.for(binder.model.notes).value = 'foo';

    assert.equal(foo, 'baz');
  });

  describe('name value', () => {
    let binder: Binder<Order, OrderModel>;

    const expectedEmptyOrder: Order = {
      idString: '',
      customer: {
        idString: '',
        fullName: '',
        nickName: '',
      },
      notes: '',
      priority: 0,
      products: [],
    };

    beforeEach(() => {
      binder = new Binder(litOrderView, OrderModel);
      requestUpdateStub.reset();
    });

    it('should have name for models', () => {
      assert.equal(binder.for(binder.model.notes).name, 'notes');
      assert.equal(binder.for(binder.model.customer.fullName).name, 'customer.fullName');
    });

    it('should have initial defaultValue', () => {
      assert.deepEqual(binder.defaultValue, expectedEmptyOrder);
    });

    it('should have valueOf', () => {
      assert.equal(binder.model.notes.valueOf(), '');
      assert.equal(binder.model.priority.valueOf(), 0);
    });

    it('should have toString', () => {
      assert.equal(binder.model.notes.valueOf(), '');
      assert.equal(binder.model.priority.toString(), '0');
    });

    it('should have initial value', () => {
      assert.equal(binder.value, binder.defaultValue);
      assert.equal(binder.for(binder.model).value, binder.value);
      assert.equal(binder.for(binder.model.notes).value, '');
      assert.equal(binder.for(binder.model.customer.fullName).value, '');
      assert.equal(binder.model.valueOf(), binder.value);
      assert.equal(binder.model.notes.valueOf(), '');
      assert.equal(binder.model.customer.fullName.valueOf(), '');
    });

    it('should change value on setValue', () => {
      // Sanity check: requestUpdate should not be called
      expect(requestUpdateStub).to.not.be.called;

      binder.for(binder.model.notes).value = 'foo';
      assert.equal(binder.value.notes, 'foo');
      expect(requestUpdateStub).to.be.calledOnce;
    });

    it('should change value on deep setValue', () => {
      expect(requestUpdateStub).to.not.be.called;

      binder.for(binder.model.customer.fullName).value = 'foo';
      assert.equal(binder.value.customer.fullName, 'foo');
      expect(requestUpdateStub).to.be.calledOnce;
    });

    it('should not change defaultValue on setValue', () => {
      binder.for(binder.model.notes).value = 'foo';
      binder.for(binder.model.customer.fullName).value = 'foo';

      assert.equal(binder.defaultValue.notes, '');
      assert.equal(binder.defaultValue.customer.fullName, '');
    });

    it('should reset to default value', () => {
      binder.for(binder.model.notes).value = 'foo';
      binder.for(binder.model.customer.fullName).value = 'foo';
      requestUpdateStub.reset();

      binder.reset();

      assert.equal(binder.value.notes, '');
      assert.equal(binder.value.customer.fullName, '');
      expect(requestUpdateStub).to.be.calledOnce;
    });

    it('should reset to provided value', () => {
      binder.for(binder.model.notes).value = 'foo';
      binder.for(binder.model.customer.fullName).value = 'foo';
      requestUpdateStub.reset();

      binder.read({
        ...expectedEmptyOrder,
        notes: 'bar',
        customer: {
          ...expectedEmptyOrder.customer,
          fullName: 'bar',
        },
      });

      assert.equal(binder.value.notes, 'bar');
      assert.equal(binder.value.customer.fullName, 'bar');
      expect(requestUpdateStub).to.be.calledOnce;
    });

    it('should clear value and default value', () => {
      binder.read({
        ...expectedEmptyOrder,
        notes: 'bar',
        customer: {
          ...expectedEmptyOrder.customer,
          fullName: 'bar',
        },
      });
      requestUpdateStub.reset();
      assert.notDeepEqual(binder.value, expectedEmptyOrder);
      assert.notDeepEqual(binder.defaultValue, expectedEmptyOrder);

      binder.clear();

      assert.deepEqual(binder.value, expectedEmptyOrder);
      assert.deepEqual(binder.defaultValue, expectedEmptyOrder);
      expect(requestUpdateStub).to.be.calledOnce;
    });

    it('should update when clearing validation', async () => {
      binder.clear();
      const binderNode = binder.for(binder.model.customer.fullName);
      await binderNode.validate();
      assert.isTrue(binderNode.invalid);
      requestUpdateStub.reset();

      binder.clear();

      assert.isFalse(binderNode.invalid);
      expect(requestUpdateStub).to.be.calledOnce;
    });

    it('should not update excessively when nothing to clear', async () => {
      binder.clear();
      const binderNode = binder.for(binder.model.customer.fullName);
      await binderNode.validate();
      assert.isTrue(binderNode.invalid);
      binder.clear();
      requestUpdateStub.reset();

      binder.clear();
      expect(requestUpdateStub).to.not.be.called;
    });

    it('should forget visits on clear', () => {
      const binderNode = binder.for(binder.model.customer.fullName);
      binderNode.visited = true;

      binder.clear();

      assert.isFalse(binderNode.visited);
    });

    it('should be able to set null to object type property', () => {
      const myBinder: Binder<TestEntity, TestModel<TestEntity>> = new Binder(document.createElement('div'), TestModel);
      myBinder.for(myBinder.model.fieldAny).value = null;
      myBinder.for(myBinder.model.fieldAny).validate();
      assert.isFalse(myBinder.invalid);
    });

    it('should be able to set undefined to object type property', () => {
      const myBinder: Binder<TestEntity, TestModel<TestEntity>> = new Binder(document.createElement('div'), TestModel);
      myBinder.for(myBinder.model.fieldAny).value = undefined;
      myBinder.for(myBinder.model.fieldAny).validate();
      assert.isFalse(myBinder.invalid);
    });
  });

  describe('optional', () => {
    let binder: Binder<Employee, EmployeeModel<Employee>>;
    const litEmployeeView = document.createElement('lit-employee-view') as LitEmployeeView;

    const expectedEmptyEmployee: Employee = {
      idString: '',
      fullName: '',
      supervisor: undefined,
      colleagues: undefined,
    };

    beforeEach(() => {
      binder = new Binder(litEmployeeView, EmployeeModel);
    });

    function getOnlyEmployeeData(e: Employee) {
      const { idString, fullName } = e;
      return { idString, fullName };
    }

    it('should not initialize optional in empty value', () => {
      const emptyValue = EmployeeModel.createEmptyValue();
      assert.isUndefined(emptyValue.supervisor);
    });

    it('should not initialize optional in binder value and default value', () => {
      assert.isUndefined(binder.defaultValue.supervisor);
      assert.deepEqual(binder.defaultValue, expectedEmptyEmployee);
      // Ensure the key is present in the object
      assert.isTrue('supervisor' in binder.defaultValue);
      assert.isUndefined(binder.value.supervisor);
      assert.deepEqual(binder.value, expectedEmptyEmployee);
      assert.isTrue('supervisor' in binder.value);
    });

    it('should not initialize optional on binderNode access', () => {
      binder.for(binder.model.supervisor);

      assert.isUndefined(binder.defaultValue.supervisor);
      assert.deepEqual(binder.defaultValue, expectedEmptyEmployee);
      assert.isTrue('supervisor' in binder.defaultValue);
      assert.isUndefined(binder.value.supervisor);
      assert.deepEqual(binder.value, expectedEmptyEmployee);
      assert.isTrue('supervisor' in binder.value);
    });

    it('should initialize parent optional on child binderNode access', () => {
      binder.for(binder.model.supervisor.supervisor);

      assert.isDefined(binder.defaultValue.supervisor);
      assert.deepEqual(binder.defaultValue.supervisor, expectedEmptyEmployee);
      assert.deepEqual(getOnlyEmployeeData(binder.defaultValue), getOnlyEmployeeData(expectedEmptyEmployee));
      assert.isDefined(binder.value.supervisor);
      assert.deepEqual(binder.value.supervisor, expectedEmptyEmployee);
      assert.deepEqual(getOnlyEmployeeData(binder.value), getOnlyEmployeeData(expectedEmptyEmployee));
    });

    it('should not become dirty on binderNode access', () => {
      assert.isFalse(binder.dirty);

      binder.for(binder.model.supervisor);
      assert.isFalse(binder.dirty);

      binder.for(binder.model.supervisor.supervisor);
      assert.isFalse(binder.dirty);
    });

    it('should not fail validation for non-initialised object or array', async () => {
      await binder.validate();
      assert.isFalse(binder.invalid);

      // Populate non-initialised optional field with data
      binder.value = { ...binder.value, supervisor: expectedEmptyEmployee };

      await binder.validate();
      assert.isFalse(binder.invalid);

      // Populate non-initialised optional field with deep optional data
      binder.value = {
        ...binder.value,
        supervisor: {
          ...expectedEmptyEmployee,
          supervisor: expectedEmptyEmployee,
        },
      };

      await binder.validate();
      assert.isFalse(binder.invalid);
    });

    it('should allow to reset optional object or array', async () => {
      // Start from fields with optional data
      binder.read({
        ...binder.value,
        supervisor: expectedEmptyEmployee,
        colleagues: [expectedEmptyEmployee],
      });

      await binder.validate();
      assert.isFalse(binder.invalid);

      // Reset optionals back to undefined
      binder.value = expectedEmptyEmployee;

      await binder.validate();
      assert.isFalse(binder.invalid);
    });

    // https://github.com/vaadin/fusion/issues/43
    it('should be able to bind to a nested property of an optional parent', async () => {
      const superNameNode = binder.for(binder.model.supervisor.fullName);
      binder.read({
        ...expectedEmptyEmployee,
      });
      assert.equal('', superNameNode.value);
    });

    // https://github.com/vaadin/fusion/issues/43
    it('should be able to read a nested property of an optional parent after clear', async () => {
      const superNameNode = binder.for(binder.model.supervisor.fullName);
      binder.clear();
      assert.equal('', superNameNode.value);
    });
  });
});
