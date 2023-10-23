import { expect, use } from '@esm-bundle/chai';
import { render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import chaiAsPromised from 'chai-as-promised';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { ExperimentalAutoForm } from '../src/autoform.js';
import type { CrudService } from '../src/crud.js';
import FormController from './FormController.js';
import {
  createService,
  getItem,
  type HasTestInfo,
  type Person,
  personData,
  PersonModel,
  personService,
} from './test-models-and-services.js';

use(sinonChai);
use(chaiAsPromised);

const DEFAULT_ERROR_MESSAGE = 'Something went wrong, please check all your values';
describe('@hilla/react-crud', () => {
  describe('Auto form', () => {
    const LABELS = ['First name', 'Last name', 'Email', 'Some integer', 'Some decimal'] as const;
    const KEYS = ['firstName', 'lastName', 'email', 'someInteger', 'someDecimal'] as ReadonlyArray<keyof Person>;
    const DEFAULT_PERSON: Person = {
      firstName: '',
      lastName: '',
      email: '',
      someInteger: 0,
      someDecimal: 0,
      id: -1,
      version: -1,
      vip: false,
    };
    let user: ReturnType<(typeof userEvent)['setup']>;

    function getExpectedValues(person: Person) {
      return (Object.entries(person) as ReadonlyArray<[keyof Person, Person[keyof Person]]>)
        .filter(([key]) => KEYS.includes(key))
        .map(([, value]) => value.toString());
    }

    beforeEach(() => {
      user = userEvent.setup();
    });

    it('renders fields for the properties in the form', async () => {
      const person: Person = {
        id: 1,
        version: 1,
        firstName: 'first',
        lastName: 'last',
        email: 'first.last@domain.com',
        someInteger: 24451,
        someDecimal: 24.451,
        vip: false,
      };

      const form = await FormController.init(
        render(<ExperimentalAutoForm service={personService()} model={PersonModel} item={person} />),
        user,
      );

      await expect(form.getValues(LABELS)).to.eventually.be.deep.equal(getExpectedValues(person));

      const fields = await form.getFields(...LABELS);
      const tagNames = fields.map((field) => field.instance.localName);
      expect(tagNames).to.eql([
        'vaadin-text-field',
        'vaadin-text-field',
        'vaadin-text-field',
        'vaadin-integer-field',
        'vaadin-number-field',
      ]);
    });

    it('works without an item', async () => {
      const form = await FormController.init(
        render(<ExperimentalAutoForm service={personService()} model={PersonModel} />),
        user,
      );
      await expect(form.getValues(LABELS)).to.eventually.be.deep.equal(getExpectedValues(DEFAULT_PERSON));
    });

    it('uses values from an item', async () => {
      const service = personService();
      const person = (await getItem(service, 2))!;

      const form = await FormController.init(
        render(<ExperimentalAutoForm service={service} model={PersonModel} item={person} />),
        user,
      );
      await expect(form.getValues(LABELS)).to.eventually.be.deep.equal(getExpectedValues(person));
    });

    it('updates values when changing item', async () => {
      const service = personService();
      const person1 = (await getItem(service, 2))!;
      const person2 = (await getItem(service, 1))!;

      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} item={person1} />);
      let form = await FormController.init(result, user);
      await expect(form.getValues(LABELS)).to.eventually.be.deep.equal(getExpectedValues(person1));

      result.rerender(<ExperimentalAutoForm service={service} model={PersonModel} item={person2} />);
      form = await FormController.init(result, user);
      await expect(form.getValues(LABELS)).to.eventually.be.deep.equal(getExpectedValues(person2));
    });

    it('clears the form when setting the item to undefined', async () => {
      const service = personService();
      const person = (await getItem(service, 2))!;

      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} item={person} />);
      let form = await FormController.init(result, user);
      await expect(form.getValues(LABELS)).to.eventually.be.deep.equal(getExpectedValues(person));

      result.rerender(<ExperimentalAutoForm service={service} model={PersonModel} item={undefined} />);
      form = await FormController.init(result, user);
      await expect(form.getValues(LABELS)).to.eventually.be.deep.equal(getExpectedValues(DEFAULT_PERSON));
    });

    it('submits a valid form', async () => {
      const service: CrudService<Person> & HasTestInfo = createService<Person>(personData);
      const saveSpy = sinon.spy(service, 'save');

      const form = await FormController.init(
        render(<ExperimentalAutoForm service={service} model={PersonModel} item={undefined} />),
        user,
      );
      await form.typeInField('First name', 'Joe');
      await form.typeInField('Last name', 'Quinby');
      await form.typeInField('Some integer', '12');
      await form.typeInField('Some decimal', '0.12');
      await form.submit();

      expect(saveSpy).to.have.been.calledOnce;
      const newItem = saveSpy.getCall(0).args[0];
      expect(newItem.firstName).to.equal('Joe');
      expect(newItem.lastName).to.equal('Quinby');
      expect(newItem.someInteger).to.equal(12);
      expect(newItem.someDecimal).to.equal(0.12);
    });

    it('retains the form values a valid submit', async () => {
      const service: CrudService<Person> & HasTestInfo = createService<Person>(personData);
      const person = await getItem(service, 1);
      const form = await FormController.init(
        render(<ExperimentalAutoForm service={service} model={PersonModel} item={person} />),
        user,
      );
      await form.typeInField('First name', 'bar');
      await form.submit();
      const newPerson: Person = { ...person! };
      newPerson.firstName = 'bar';
      await expect(form.getValues(LABELS)).to.eventually.be.deep.equal(getExpectedValues(newPerson));
    });
    it('retains the form values after a valid submit when using afterSubmit', async () => {
      const service: CrudService<Person> & HasTestInfo = createService<Person>(personData);
      const person = await getItem(service, 1);
      const submitSpy = sinon.spy();

      const form = await FormController.init(
        render(<ExperimentalAutoForm service={service} model={PersonModel} item={person} afterSubmit={submitSpy} />),
        user,
      );
      await form.typeInField('First name', 'baz');
      await form.submit();
      const newPerson: Person = { ...person! };
      newPerson.firstName = 'baz';
      await expect(form.getValues(LABELS)).to.eventually.be.deep.equal(getExpectedValues(newPerson));
    });

    it('calls afterSubmit with the new item', async () => {
      const service: CrudService<Person> & HasTestInfo = createService<Person>(personData);
      const person = await getItem(service, 1);
      const submitSpy = sinon.spy();

      const form = await FormController.init(
        render(<ExperimentalAutoForm service={service} model={PersonModel} item={person} afterSubmit={submitSpy} />),
        user,
      );
      await form.typeInField('First name', 'bag');
      await form.submit();
      expect(submitSpy).to.have.been.calledWithMatch(sinon.match.hasNested('item.firstName', 'bag'));
    });

    it('shows an error if the endpoint call fails', async () => {
      const service: CrudService<Person> & HasTestInfo = createService<Person>(personData);
      // eslint-disable-next-line @typescript-eslint/require-await
      service.save = async (item: Person): Promise<Person | undefined> => {
        throw new Error('foobar');
      };
      const person = await getItem(service, 1);
      const submitSpy = sinon.spy();

      const result = render(
        <ExperimentalAutoForm service={service} model={PersonModel} item={person} afterSubmit={submitSpy} />,
      );
      const form = await FormController.init(result, user);
      await form.typeInField('First name', 'J'); // to enable the submit button
      await form.submit();
      expect(submitSpy).to.have.not.been.called;
      expect(result.queryByText(DEFAULT_ERROR_MESSAGE)).to.not.be.null;
    });

    it('calls afterSubmitError and does not show error if the endpoint call fails', async () => {
      const service: CrudService<Person> & HasTestInfo = createService<Person>(personData);
      // eslint-disable-next-line @typescript-eslint/require-await
      service.save = async (item: Person): Promise<Person | undefined> => {
        throw new Error('foobar');
      };
      const person = await getItem(service, 1);
      const errorSpy = sinon.spy();
      const submitSpy = sinon.spy();
      const result = render(
        <ExperimentalAutoForm
          service={service}
          model={PersonModel}
          item={person}
          afterSubmit={submitSpy}
          onSubmitError={errorSpy}
        />,
      );
      const form = await FormController.init(result, user);
      await form.typeInField('First name', 'J'); // to enable the submit button
      await form.submit();
      expect(result.queryByText(DEFAULT_ERROR_MESSAGE)).to.be.null;
      expect(submitSpy).to.have.not.been.called;
      expect(errorSpy).to.have.been.calledWith(sinon.match.hasNested('error.message', 'foobar'));
    });

    it('disables all fields and buttons when disabled', async () => {
      const form = await FormController.init(
        render(<ExperimentalAutoForm service={personService()} model={PersonModel} disabled />),
        user,
      );
      await expect(form.areEnabled(LABELS)).to.eventually.be.false;
    });

    it('enables all fields and buttons when enabled', async () => {
      const service = personService();
      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} disabled />);
      await FormController.init(result, user);
      result.rerender(<ExperimentalAutoForm service={service} model={PersonModel} />);
      const form = await FormController.init(result, user);
      await expect(form.areEnabled(LABELS)).to.eventually.be.true;
    });

    describe('discard button', () => {
      it('does not show a discard button if the form is not dirty', async () => {
        const form = await FormController.init(
          render(<ExperimentalAutoForm service={personService()} model={PersonModel} />),
          user,
        );
        await expect(form.findButton('Discard')).to.eventually.be.rejected;
      });

      it('does show a discard button if the form is dirty', async () => {
        const form = await FormController.init(
          render(<ExperimentalAutoForm service={personService()} model={PersonModel} />),
          user,
        );
        await form.typeInField('First name', 'foo');
        await expect(form.findButton('Discard')).to.eventually.exist;
      });

      it('resets the form when clicking the discard button', async () => {
        const form = await FormController.init(
          render(<ExperimentalAutoForm service={personService()} model={PersonModel} />),
          user,
        );
        await form.typeInField('First name', 'foo');
        await expect(form.findButton('Discard')).to.eventually.exist;

        await form.discard();
        await expect(form.getValues(['First name'])).to.eventually.eql(['']);
        await expect(form.findButton('Discard')).to.eventually.be.rejected;
      });
    });

    it('when creating new, submit button is enabled at the beginning', async () => {
      const service = personService();
      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} />);
      const form = await FormController.init(result, user);

      const submitButton = await form.findButton('Submit');
      expect(submitButton.disabled).to.be.false;
    });

    it('passing null interprets as creating new, submit button is enabled at the beginning', async () => {
      const service = personService();
      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} item={null} />);
      const form = await FormController.init(result, user);

      const submitButton = await form.findButton('Submit');
      expect(submitButton.disabled).to.be.false;
    });

    it('passing undefined interprets as creating new, submit button is enabled at the beginning', async () => {
      const service = personService();
      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} item={undefined} />);
      const form = await FormController.init(result, user);

      const submitButton = await form.findButton('Submit');
      expect(submitButton.disabled).to.be.false;
    });

    it('when editing, submit button remains disabled before any changes', async () => {
      const service = personService();
      const person = await getItem(service, 1);
      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} item={person} />);
      const form = await FormController.init(result, user);

      const submitButton = await form.findButton('Submit');
      expect(submitButton.disabled).to.be.true;
    });

    it('when editing, submit button becomes disabled again when the form is reset', async () => {
      const service = personService();
      const person = await getItem(service, 1);
      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} item={person} />);
      const form = await FormController.init(result, user);
      const submitButton = await form.findButton('Submit');

      await form.typeInField('First name', 'J'); // to enable the submit button
      expect(submitButton.disabled).to.be.false;

      await form.discard();
      expect(submitButton.disabled).to.be.true;
    });
  });
});
