import { expect, use } from '@esm-bundle/chai';
import type { TextFieldElement } from '@hilla/react-components/TextField.js';
import { render, type RenderResult } from '@testing-library/react';
import { assert } from 'chai';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { ExperimentalAutoForm } from '../src/autoform.js';
import type { CrudService } from '../src/crud.js';
import type Sort from '../src/types/dev/hilla/mappedtypes/Sort.js';
import {
  PersonModel,
  createService,
  personData,
  personService,
  type HasLastFilter,
  type Person,
} from './test-models-and-services.js';

use(sinonChai);

const noSort: Sort = { orders: [] };

async function getItem(service: CrudService<Person> & HasLastFilter, id: number) {
  return (await service.list({ pageNumber: 0, pageSize: 1000, sort: noSort }, undefined)).find((p) => p.id === id);
}

export async function nextFrame(): Promise<void> {
  return new Promise((resolve) => {
    requestAnimationFrame(() => {
      resolve(undefined);
    });
  });
}
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
async function submit(result: RenderResult) {
  const submitButton = await result.findByText('Submit');
  submitButton.click();
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
      const result = render(<ExperimentalAutoForm service={personService} model={PersonModel} item={person} />);
      await assertFormFieldValue(result, 'First name', person.firstName);
      await assertFormFieldValue(result, 'Last name', person.lastName);
      await assertFormFieldValue(result, 'Email', person.email);
      await assertFormFieldValue(result, 'Some number', person.someNumber);
    });
    it('works without an item', async () => {
      const result = render(<ExperimentalAutoForm service={personService} model={PersonModel} />);
      await assertFormFieldValue(result, 'First name', '');
      await assertFormFieldValue(result, 'Last name', '');
      await assertFormFieldValue(result, 'Email', '');
      await assertFormFieldValue(result, 'Some number', 0);
    });
    it('uses values from an item', async () => {
      const person = (await getItem(personService, 2))!;

      const result = render(<ExperimentalAutoForm service={personService} model={PersonModel} item={person} />);
      await assertFormFieldValue(result, 'First name', person.firstName);
      await assertFormFieldValue(result, 'Last name', person.lastName);
      await assertFormFieldValue(result, 'Email', person.email);
      await assertFormFieldValue(result, 'Some number', person.someNumber);
    });
    it('updates values when changing item', async () => {
      const person1 = (await getItem(personService, 2))!;
      const person2 = (await getItem(personService, 1))!;

      const result = render(<ExperimentalAutoForm service={personService} model={PersonModel} item={person1} />);
      await assertFormFieldValue(result, 'First name', person1.firstName);
      await assertFormFieldValue(result, 'Last name', person1.lastName);
      await assertFormFieldValue(result, 'Email', person1.email);
      await assertFormFieldValue(result, 'Some number', person1.someNumber);

      result.rerender(<ExperimentalAutoForm service={personService} model={PersonModel} item={person2} />);
      await assertFormFieldValue(result, 'First name', person2.firstName);
      await assertFormFieldValue(result, 'Last name', person2.lastName);
      await assertFormFieldValue(result, 'Email', person2.email);
      await assertFormFieldValue(result, 'Some number', person2.someNumber);
    });
    it('clears the form when setting the item to undefined', async () => {
      const person = (await getItem(personService, 2))!;

      const result = render(<ExperimentalAutoForm service={personService} model={PersonModel} item={person} />);
      await assertFormFieldValue(result, 'First name', person.firstName);
      await assertFormFieldValue(result, 'Last name', person.lastName);
      await assertFormFieldValue(result, 'Email', person.email);
      await assertFormFieldValue(result, 'Some number', person.someNumber);

      result.rerender(<ExperimentalAutoForm service={personService} model={PersonModel} item={undefined} />);
      await assertFormFieldValue(result, 'First name', '');
      await assertFormFieldValue(result, 'Last name', '');
      await assertFormFieldValue(result, 'Email', '');
      await assertFormFieldValue(result, 'Some number', 0);
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
    it('clears the form after a valid submit', async () => {
      const service: CrudService<Person> & HasLastFilter = createService<Person>(personData);
      const person = await getItem(service, 1);
      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} item={person} />);
      await setFormField(result, 'First name', 'bar');
      await submit(result);
      await nextFrame();
      await assertFormFieldValue(result, 'First name', '');
      await assertFormFieldValue(result, 'Last name', '');
      await assertFormFieldValue(result, 'Email', '');
      await assertFormFieldValue(result, 'Some number', 0);
    });
    it('clears the form after a valid submit when using onSubmit', async () => {
      const service: CrudService<Person> & HasLastFilter = createService<Person>(personData);
      const person = await getItem(service, 1);
      const submitSpy = sinon.spy();
      const result = render(
        <ExperimentalAutoForm service={service} model={PersonModel} item={person} onSubmit={submitSpy} />,
      );
      await setFormField(result, 'First name', 'baz');
      await submit(result);
      await nextFrame();
      await assertFormFieldValue(result, 'First name', '');
      await assertFormFieldValue(result, 'Last name', '');
      await assertFormFieldValue(result, 'Email', '');
      await assertFormFieldValue(result, 'Some number', 0);
    });
    it('calls onSubmit with the new item', async () => {
      const service: CrudService<Person> & HasLastFilter = createService<Person>(personData);

      const person = await getItem(service, 1);
      let submittedItem: Person | undefined;
      const result = render(
        <ExperimentalAutoForm
          service={service}
          model={PersonModel}
          item={person}
          onSubmit={({ item }) => {
            submittedItem = item;
          }}
        />,
      );
      await setFormField(result, 'First name', 'bag');
      await submit(result);

      await nextFrame();
      expect(submittedItem!.firstName).to.equal('bag');
    });
    it('shows an error if the endpoint call fails', async () => {
      const service: CrudService<Person> & HasLastFilter = createService<Person>(personData);
      service.save = async (item: Person): Promise<Person | undefined> => {
        throw new Error('foobar');
      };
      const person = await getItem(service, 1);
      const submitSpy = sinon.spy();
      const result = render(
        <ExperimentalAutoForm service={service} model={PersonModel} item={person} onSubmit={submitSpy} />,
      );
      await submit(result);
      await nextFrame();

      assert(submitSpy.notCalled);
      expect(result.queryByText(DEFAULT_ERROR_MESSAGE)).not.to.be.null;
    });
    it('calls onSubmitError and does not show error if the endpoint call fails', async () => {
      const service: CrudService<Person> & HasLastFilter = createService<Person>(personData);
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
          onSubmit={submitSpy}
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
  });
});
