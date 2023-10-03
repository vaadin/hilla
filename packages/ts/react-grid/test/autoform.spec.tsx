import { expect, use } from '@esm-bundle/chai';
import type { ButtonElement } from '@hilla/react-components/Button.js';
import type { TextFieldElement } from '@hilla/react-components/TextField.js';
import { render, type RenderResult } from '@testing-library/react';
import { assert } from 'chai';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { ExperimentalAutoForm } from '../src/autoform.js';
import type { CrudService } from '../src/crud.js';
import { submit } from './form-test-utils.js';
import {
  PersonModel,
  createService,
  getItem,
  personData,
  personService,
  type HasLastFilter,
  type Person,
} from './test-models-and-services.js';

use(sinonChai);

export async function nextFrame(): Promise<void> {
  return new Promise((resolve) => {
    requestAnimationFrame(() => {
      resolve(undefined);
    });
  });
}

// eslint-disable-next-line @typescript-eslint/require-await
async function getFormField(result: RenderResult, label: string): Promise<TextFieldElement | undefined> {
  return result.queryByLabelText(label)?.parentElement as TextFieldElement;
}

async function setFormField(result: RenderResult, label: string, value: string) {
  const field = (await getFormField(result, label))!;
  field.value = value;
  field.dispatchEvent(new CustomEvent('input'));
  await nextFrame();
}

async function assertFormFieldValue(result: RenderResult, fieldLabel: string, expected: number | string) {
  const field = (await getFormField(result, fieldLabel))!;
  expect(field.value).to.equal(expected);
}
async function assertFormFieldValues(result: RenderResult, expected: Person | undefined) {
  const person = expected ?? { firstName: '', lastName: '', email: '', someNumber: 0, id: -1, version: -1, vip: false };
  await assertFormFieldValue(result, 'First name', person.firstName);
  await assertFormFieldValue(result, 'Last name', person.lastName);
  await assertFormFieldValue(result, 'Email', person.email);
  await assertFormFieldValue(result, 'Some number', person.someNumber);
}
async function assertAllEnabled(result: RenderResult, enabled: boolean) {
  expect(!(await getFormField(result, 'First name'))!.disabled).to.equal(enabled);
  expect(!(await getFormField(result, 'Last name'))!.disabled).to.equal(enabled);
  expect(!(await getFormField(result, 'Email'))!.disabled).to.equal(enabled);
  expect(!(await getFormField(result, 'Some number'))!.disabled).to.equal(enabled);
  result.container
    .querySelectorAll('vaadin-button')
    .forEach((button: ButtonElement) => expect(!button.disabled).to.equal(enabled));
}

const DEFAULT_ERROR_MESSAGE = 'Something went wrong, please check all your values';
describe('@hilla/react-grid', () => {
  describe('Auto form', () => {
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
      const result = render(<ExperimentalAutoForm service={personService()} model={PersonModel} item={person} />);
      await assertFormFieldValues(result, person);
    });
    it('works without an item', async () => {
      const result = render(<ExperimentalAutoForm service={personService()} model={PersonModel} />);
      await assertFormFieldValues(result, undefined);
    });
    it('uses values from an item', async () => {
      const service = personService();
      const person = (await getItem(service, 2))!;

      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} item={person} />);
      await assertFormFieldValues(result, person);
    });
    it('updates values when changing item', async () => {
      const service = personService();
      const person1 = (await getItem(service, 2))!;
      const person2 = (await getItem(service, 1))!;

      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} item={person1} />);
      await assertFormFieldValues(result, person1);

      result.rerender(<ExperimentalAutoForm service={service} model={PersonModel} item={person2} />);
      await assertFormFieldValues(result, person2);
    });
    it('clears the form when setting the item to undefined', async () => {
      const service = personService();
      const person = (await getItem(service, 2))!;

      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} item={person} />);
      await assertFormFieldValues(result, person);

      result.rerender(<ExperimentalAutoForm service={service} model={PersonModel} item={undefined} />);
      await assertFormFieldValues(result, undefined);
    });
    it('submits a valid form', async () => {
      const service: CrudService<Person> & HasLastFilter = createService<Person>(personData);

      const person = await getItem(service, 1);

      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} item={person} />);
      await setFormField(result, 'First name', 'foo');
      await submit(result);
      await nextFrame();
      const newItem = await getItem(service, 1);
      expect(newItem!.firstName).to.equal('foo');
    });
    it('retains the form values a valid submit', async () => {
      const service: CrudService<Person> & HasLastFilter = createService<Person>(personData);
      const person = await getItem(service, 1);
      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} item={person} />);
      await setFormField(result, 'First name', 'bar');
      await submit(result);
      await nextFrame();
      await nextFrame();
      const newPerson: Person = { ...person! };
      newPerson.firstName = 'bar';
      await assertFormFieldValues(result, newPerson);
    });
    it('retains the form values after a valid submit when using afterSubmit', async () => {
      const service: CrudService<Person> & HasLastFilter = createService<Person>(personData);
      const person = await getItem(service, 1);
      const submitSpy = sinon.spy();
      const result = render(
        <ExperimentalAutoForm service={service} model={PersonModel} item={person} afterSubmit={submitSpy} />,
      );
      await setFormField(result, 'First name', 'baz');
      await submit(result);
      await nextFrame();
      const newPerson: Person = { ...person! };
      newPerson.firstName = 'baz';
      await assertFormFieldValues(result, newPerson);
    });
    it('calls afterSubmit with the new item', async () => {
      const service: CrudService<Person> & HasLastFilter = createService<Person>(personData);

      const person = await getItem(service, 1);
      const submitSpy = sinon.spy();
      const result = render(
        <ExperimentalAutoForm service={service} model={PersonModel} item={person} afterSubmit={submitSpy} />,
      );
      await setFormField(result, 'First name', 'bag');
      await submit(result);

      await nextFrame();
      await nextFrame();

      assert(submitSpy.calledWithMatch(sinon.match.hasNested('item.firstName', 'bag')));
    });
    it('shows an error if the endpoint call fails', async () => {
      const service: CrudService<Person> & HasLastFilter = createService<Person>(personData);
      // eslint-disable-next-line @typescript-eslint/require-await
      service.save = async (item: Person): Promise<Person | undefined> => {
        throw new Error('foobar');
      };
      const person = await getItem(service, 1);
      const submitSpy = sinon.spy();
      const result = render(
        <ExperimentalAutoForm service={service} model={PersonModel} item={person} afterSubmit={submitSpy} />,
      );
      await submit(result);
      await nextFrame();
      await nextFrame();

      assert(submitSpy.notCalled);
      expect(result.queryByText(DEFAULT_ERROR_MESSAGE)).not.to.be.null;
    });
    it('calls afterSubmitError and does not show error if the endpoint call fails', async () => {
      const service: CrudService<Person> & HasLastFilter = createService<Person>(personData);
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
      await submit(result);
      await nextFrame();
      // eslint-disable-next-line
      expect(result.queryByText(DEFAULT_ERROR_MESSAGE)).to.be.null;
      assert(submitSpy.notCalled);
      assert(errorSpy.calledWith(sinon.match.hasNested('error.message', 'foobar')));
    });
    it('disables all fields and buttons when disabled', async () => {
      const result = render(<ExperimentalAutoForm service={personService()} model={PersonModel} disabled />);
      await nextFrame();
      await assertAllEnabled(result, false);
    });
    it('enables all fields and buttons when enabled', async () => {
      const service = personService();
      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} disabled />);
      await nextFrame();
      result.rerender(<ExperimentalAutoForm service={service} model={PersonModel} />);
      await nextFrame();
      await nextFrame();
      await assertAllEnabled(result, true);
    });
  });
});
