import { expect, use } from '@esm-bundle/chai';
import type { TextFieldElement } from '@hilla/react-components/TextField.js';
import { render, type RenderResult } from '@testing-library/react';
import sinonChai from 'sinon-chai';
import { AutoForm } from '../src/autoform.js';
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
async function getFormField(result: RenderResult, label: string): Promise<TextFieldElement> {
  return (await result.findByLabelText(label)).parentElement as TextFieldElement;
}
async function setFormField(result: RenderResult, label: string, value: string) {
  const field = await getFormField(result, label);
  field.value = value;
  field.dispatchEvent(new CustomEvent('input'));
  await nextFrame();
}
async function assertFormFieldValue(result: RenderResult, fieldLabel: string, expected: number | string) {
  const field = await getFormField(result, fieldLabel);
  expect(field.value).to.equal(expected);
}
async function submit(result: RenderResult) {
  const submitButton = await result.findByText('Submit');
  submitButton.click();
}

describe('@hilla/react-grid', () => {
  describe('Auto form', () => {
    it('renders fields for the properties in the form', async () => {
      const person: Person = {
        id: 1,
        firstName: 'first',
        lastName: 'last',
        email: 'first.last@domain.com',
        someNumber: 24451,
        vip: false,
      };
      const result = render(<AutoForm service={personService} model={PersonModel} item={person} />);
      await assertFormFieldValue(result, 'First name', person.firstName);
      await assertFormFieldValue(result, 'Last name', person.lastName);
      await assertFormFieldValue(result, 'Email', person.email);
      await assertFormFieldValue(result, 'Some number', person.someNumber);
    });
    it('works without an item', async () => {
      const result = render(<AutoForm service={personService} model={PersonModel} />);
      await assertFormFieldValue(result, 'First name', '');
      await assertFormFieldValue(result, 'Last name', '');
      await assertFormFieldValue(result, 'Email', '');
      await assertFormFieldValue(result, 'Some number', 0);
    });
    it('uses values from an item', async () => {
      const person = (await getItem(personService, 2))!;

      const result = render(<AutoForm service={personService} model={PersonModel} item={person} />);
      await assertFormFieldValue(result, 'First name', person.firstName);
      await assertFormFieldValue(result, 'Last name', person.lastName);
      await assertFormFieldValue(result, 'Email', person.email);
      await assertFormFieldValue(result, 'Some number', person.someNumber);
    });
    it('submits a valid form', async () => {
      const service: CrudService<Person> & HasLastFilter = createService<Person>(personData);

      const person = await getItem(service, 1);

      const result = render(<AutoForm service={service} model={PersonModel} item={person} />);
      await setFormField(result, 'First name', 'foo');
      await submit(result);
      await nextFrame();
      const newItem = await getItem(service, 1);
      expect(newItem!.firstName).to.equal('foo');
    });

    it('calls onSubmit with the new item', async () => {
      const service: CrudService<Person> & HasLastFilter = createService<Person>(personData);

      const person = await getItem(service, 1);
      let submittedItem: Person | undefined;
      const result = render(
        <AutoForm
          service={service}
          model={PersonModel}
          item={person}
          onSubmit={({ item }) => {
            submittedItem = item;
          }}
        />,
      );
      await setFormField(result, 'First name', 'foo');
      await submit(result);

      await nextFrame();
      expect(submittedItem!.firstName).to.equal('foo');
    });
    it('shows an error if the endpoint call fails', async () => {
      const service: CrudService<Person> & HasLastFilter = createService<Person>(personData);
      service.update = async (item: Person): Promise<Person | undefined> => {
        throw new Error('foobar');
      };
      const person = await getItem(service, 1);
      const result = render(<AutoForm service={service} model={PersonModel} item={person} />);
      await submit(result);
      await nextFrame();
      // eslint-disable-next-line
      const formError = (result.container.querySelector('#formerror') as HTMLElement).innerText;
      expect(formError).to.equal('Something went wrong, please check all your values');
    });
    it('calls onSubmitEerror and does not show error if the endpoint call fails', async () => {
      const service: CrudService<Person> & HasLastFilter = createService<Person>(personData);
      service.update = async (item: Person): Promise<Person | undefined> => {
        throw new Error('foobar');
      };
      const person = await getItem(service, 1);
      let error;
      const result = render(
        <AutoForm
          service={service}
          model={PersonModel}
          item={person}
          onSubmitError={({ errorMessage }) => {
            error = errorMessage;
          }}
        />,
      );
      await submit(result);
      await nextFrame();
      // eslint-disable-next-line
      const formError = (result.container.querySelector('#formerror') as HTMLElement).innerText;
      expect(formError).to.equal('');
      expect(error).to.equal('Something went wrong, please check all your values');
    });
  });
});
