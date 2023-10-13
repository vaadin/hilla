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
    const LABELS = ['First name', 'Last name', 'Email', 'Some number'] as const;
    const KEYS = ['firstName', 'lastName', 'email', 'someNumber'] as ReadonlyArray<keyof Person>;
    const DEFAULT_PERSON: Person = {
      firstName: '',
      lastName: '',
      email: '',
      someNumber: 0,
      id: -1,
      version: -1,
      vip: false,
    };
    let user: ReturnType<(typeof userEvent)['setup']>;

    function getExpectedValues(person: Person) {
      return (Object.entries(person) as ReadonlyArray<[keyof Person, Person[keyof Person]]>)
        .filter(([key]) => KEYS.includes(key))
        .map(([, value]) => value);
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
        someNumber: 24451,
        vip: false,
      };

      const form = await FormController.init(
        render(<ExperimentalAutoForm service={personService()} model={PersonModel} item={person} />),
        user,
      );

      await expect(form.getValues(LABELS)).to.eventually.be.deep.equal(getExpectedValues(person));
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
      const person = await getItem(service, 1);

      const form = await FormController.init(
        render(<ExperimentalAutoForm service={service} model={PersonModel} item={person} />),
        user,
      );
      await form.typeInField('First name', 'foo');
      await form.submit();
      const newItem = await getItem(service, 1);
      expect(newItem?.firstName).to.equal('foo');
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
  });
});
