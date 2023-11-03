// eslint-disable-next-line
/// <reference types="karma-viewport" />

import { expect, use } from '@esm-bundle/chai';
import type { SelectElement } from '@hilla/react-components/Select.js';
import { TextArea } from '@hilla/react-components/TextArea.js';
import { VerticalLayout } from '@hilla/react-components/VerticalLayout.js';
import type { FieldDirectiveResult } from '@hilla/react-form';
import { render, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import chaiAsPromised from 'chai-as-promised';
import type { ComponentType } from 'react';
import type { JSX } from 'react';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { type AutoFormLayoutProps, type AutoFormLayoutRendererProps, ExperimentalAutoForm } from '../src/autoform.js';
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
  Gender,
} from './test-models-and-services.js';

use(sinonChai);
use(chaiAsPromised);

const DEFAULT_ERROR_MESSAGE = 'Something went wrong, please check all your values';
describe('@hilla/react-crud', () => {
  describe('Auto form', () => {
    const LABELS = [
      'First name',
      'Last name',
      'Gender',
      'Email',
      'Some integer',
      'Some decimal',
      'Vip',
      'Birth date',
      'Shift start',
    ] as const;
    const KEYS = [
      'firstName',
      'lastName',
      'gender',
      'email',
      'someInteger',
      'someDecimal',
      'vip',
      'birthDate',
      'shiftStart',
    ] as ReadonlyArray<keyof Person>;
    const DEFAULT_PERSON: Person = {
      firstName: '',
      lastName: '',
      gender: Gender.MALE,
      email: '',
      someInteger: 0,
      someDecimal: 0,
      id: -1,
      version: -1,
      vip: false,
      birthDate: '',
      shiftStart: '',
    };
    let user: ReturnType<(typeof userEvent)['setup']>;

    function getExpectedValues(person: Person) {
      return (Object.entries(person) as ReadonlyArray<[keyof Person, Person[keyof Person]]>)
        .filter(([key]) => KEYS.includes(key))
        .map(([, value]) => value.toString());
    }

    async function expectFieldColSpan(form: FormController, fieldName: string, expectedColSpan: string | null) {
      const formElement = await form.getField(fieldName);
      if (expectedColSpan === null) {
        return expect(formElement).to.not.have.attribute('colspan');
      }
      return expect(formElement).to.have.attribute('colspan', expectedColSpan);
    }

    async function populatePersonForm(
      personId: number,
      customLayoutRenderer?: AutoFormLayoutProps | ComponentType<AutoFormLayoutRendererProps<PersonModel>>,
      screenSize?: string,
      disabled?: boolean,
      customFields?: Record<string, (props: { field: FieldDirectiveResult }) => JSX.Element>,
    ): Promise<FormController> {
      if (screenSize) {
        viewport.set(screenSize);
      }
      const service = personService();
      const person = await getItem(service, personId);
      const result = render(
        <ExperimentalAutoForm
          service={service}
          model={PersonModel}
          item={person}
          disabled={disabled}
          customLayoutRenderer={customLayoutRenderer}
          customFields={customFields}
        />,
      );
      return await FormController.init(user, result.container);
    }

    beforeEach(() => {
      user = userEvent.setup();
    });

    afterEach(() => {
      viewport.reset();
    });

    it('renders fields for the properties in the form', async () => {
      const person: Person = {
        id: 1,
        version: 1,
        firstName: 'first',
        lastName: 'last',
        gender: Gender.FEMALE,
        email: 'first.last@domain.com',
        someInteger: 24451,
        someDecimal: 24.451,
        vip: false,
        birthDate: '1999-12-31',
        shiftStart: '08:30',
      };

      const form = await FormController.init(
        user,
        render(<ExperimentalAutoForm service={personService()} model={PersonModel} item={person} />).container,
      );

      await expect(form.getValues(...LABELS)).to.eventually.be.deep.equal(getExpectedValues(person));

      const fields = await form.getFields(...LABELS);
      const tagNames = fields.map((field) => field.localName);
      expect(tagNames).to.eql([
        'vaadin-text-field',
        'vaadin-text-field',
        'vaadin-select',
        'vaadin-text-field',
        'vaadin-integer-field',
        'vaadin-number-field',
        'vaadin-checkbox',
        'vaadin-date-picker',
        'vaadin-time-picker',
      ]);
    });

    it('works without an item', async () => {
      const form = await FormController.init(
        user,
        render(<ExperimentalAutoForm service={personService()} model={PersonModel} />).container,
      );
      await expect(form.getValues(...LABELS)).to.eventually.be.deep.equal(getExpectedValues(DEFAULT_PERSON));
    });

    it('uses values from an item', async () => {
      const service = personService();
      const person = (await getItem(service, 2))!;

      const form = await FormController.init(
        user,
        render(<ExperimentalAutoForm service={service} model={PersonModel} item={person} />).container,
      );
      await expect(form.getValues(...LABELS)).to.eventually.be.deep.equal(getExpectedValues(person));
    });

    it('updates values when changing item', async () => {
      const service = personService();
      const person1 = (await getItem(service, 2))!;
      const person2 = (await getItem(service, 1))!;

      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} item={person1} />);
      let form = await FormController.init(user, result.container);
      await expect(form.getValues(...LABELS)).to.eventually.be.deep.equal(getExpectedValues(person1));

      result.rerender(<ExperimentalAutoForm service={service} model={PersonModel} item={person2} />);
      form = await FormController.init(user, result.container);
      await expect(form.getValues(...LABELS)).to.eventually.be.deep.equal(getExpectedValues(person2));
    });

    it('clears the form when setting the item to undefined', async () => {
      const service = personService();
      const person = (await getItem(service, 2))!;

      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} item={person} />);
      let form = await FormController.init(user, result.container);
      await expect(form.getValues(...LABELS)).to.eventually.be.deep.equal(getExpectedValues(person));

      result.rerender(<ExperimentalAutoForm service={service} model={PersonModel} item={undefined} />);
      form = await FormController.init(user, result.container);
      await expect(form.getValues(...LABELS)).to.eventually.be.deep.equal(getExpectedValues(DEFAULT_PERSON));
    });

    it('submits a valid form', async () => {
      const service: CrudService<Person> & HasTestInfo = createService<Person>(personData);
      const saveSpy = sinon.spy(service, 'save');

      const form = await FormController.init(
        user,
        render(<ExperimentalAutoForm service={service} model={PersonModel} item={undefined} />).container,
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
        user,
        render(<ExperimentalAutoForm service={service} model={PersonModel} item={person} />).container,
      );
      await form.typeInField('First name', 'bar');
      await form.submit();
      const newPerson: Person = { ...person! };
      newPerson.firstName = 'bar';
      await expect(form.getValues(...LABELS)).to.eventually.be.deep.equal(getExpectedValues(newPerson));
    });
    it('retains the form values after a valid submit when using afterSubmit', async () => {
      const service: CrudService<Person> & HasTestInfo = createService<Person>(personData);
      const person = await getItem(service, 1);
      const submitSpy = sinon.spy();

      const form = await FormController.init(
        user,
        render(<ExperimentalAutoForm service={service} model={PersonModel} item={person} afterSubmit={submitSpy} />)
          .container,
      );
      await form.typeInField('First name', 'baz');
      await form.submit();
      const newPerson: Person = { ...person! };
      newPerson.firstName = 'baz';
      await expect(form.getValues(...LABELS)).to.eventually.be.deep.equal(getExpectedValues(newPerson));
    });

    it('calls afterSubmit with the new item', async () => {
      const service: CrudService<Person> & HasTestInfo = createService<Person>(personData);
      const person = await getItem(service, 1);
      const submitSpy = sinon.spy();

      const form = await FormController.init(
        user,
        render(<ExperimentalAutoForm service={service} model={PersonModel} item={person} afterSubmit={submitSpy} />)
          .container,
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
      const form = await FormController.init(user, result.container);
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
      const form = await FormController.init(user, result.container);
      await form.typeInField('First name', 'J'); // to enable the submit button
      await form.submit();
      expect(result.queryByText(DEFAULT_ERROR_MESSAGE)).to.be.null;
      expect(submitSpy).to.have.not.been.called;
      expect(errorSpy).to.have.been.calledWith(sinon.match.hasNested('error.message', 'foobar'));
    });

    it('disables all fields and buttons when disabled', async () => {
      const form = await FormController.init(
        user,
        render(<ExperimentalAutoForm service={personService()} model={PersonModel} disabled />).container,
      );
      await expect(form.areEnabled(...LABELS)).to.eventually.be.false;
    });

    it('enables all fields and buttons when enabled', async () => {
      const service = personService();
      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} disabled />);
      await FormController.init(user, result.container);
      result.rerender(<ExperimentalAutoForm service={service} model={PersonModel} />);
      const form = await FormController.init(user, result.container);
      await expect(form.areEnabled(...LABELS)).to.eventually.be.true;
    });

    describe('discard button', () => {
      it('does not show a discard button if the form is not dirty', async () => {
        const form = await FormController.init(
          user,
          render(<ExperimentalAutoForm service={personService()} model={PersonModel} />).container,
        );
        await expect(form.findButton('Discard')).to.eventually.be.rejected;
      });

      it('does show a discard button if the form is dirty', async () => {
        const form = await FormController.init(
          user,
          render(<ExperimentalAutoForm service={personService()} model={PersonModel} />).container,
        );
        await form.typeInField('First name', 'foo');
        await expect(form.findButton('Discard')).to.eventually.exist;
      });

      it('resets the form when clicking the discard button', async () => {
        const form = await FormController.init(
          user,
          render(<ExperimentalAutoForm service={personService()} model={PersonModel} />).container,
        );
        await form.typeInField('First name', 'foo');
        await expect(form.findButton('Discard')).to.eventually.exist;

        await form.discard();
        await expect(form.getValues('First name')).to.eventually.eql(['']);
        await expect(form.findButton('Discard')).to.eventually.be.rejected;
      });
    });

    it('when creating new, submit button is enabled at the beginning', async () => {
      const service = personService();
      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} />);
      const form = await FormController.init(user, result.container);

      const submitButton = await form.findButton('Submit');
      expect(submitButton.disabled).to.be.false;
    });

    it('passing null interprets as creating new, submit button is enabled at the beginning', async () => {
      const service = personService();
      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} item={null} />);
      const form = await FormController.init(user, result.container);

      const submitButton = await form.findButton('Submit');
      expect(submitButton.disabled).to.be.false;
    });

    it('passing undefined interprets as creating new, submit button is enabled at the beginning', async () => {
      const service = personService();
      const result = render(<ExperimentalAutoForm service={service} model={PersonModel} item={undefined} />);
      const form = await FormController.init(user, result.container);

      const submitButton = await form.findButton('Submit');
      expect(submitButton.disabled).to.be.false;
    });

    it('when editing, submit button remains disabled before any changes', async () => {
      const form = await populatePersonForm(1);

      const submitButton = await form.findButton('Submit');
      expect(submitButton.disabled).to.be.true;
    });

    it('when editing, submit button becomes disabled again when the form is reset', async () => {
      const form = await populatePersonForm(1);
      const submitButton = await form.findButton('Submit');

      await form.typeInField('First name', 'J'); // to enable the submit button
      expect(submitButton.disabled).to.be.false;

      await form.discard();
      expect(submitButton.disabled).to.be.true;
    });

    it('customLayoutRenderer is not defined, default two-column form layout is used', async () => {
      const form = await populatePersonForm(1);
      expect(form.formLayout.responsiveSteps).to.have.length(3);
      expect(form.formLayout.responsiveSteps).to.be.deep.equal([
        { minWidth: 0, columns: 1, labelsPosition: 'top' },
        { minWidth: '20em', columns: 1 },
        { minWidth: '40em', columns: 2 },
      ]);
      expect(form.renderResult.getElementsByTagName('vaadin-number-field')).to.have.length(1); // no Id and Version fields
      await expectFieldColSpan(form, 'First name', null);
      await expectFieldColSpan(form, 'Last name', null);
      await expectFieldColSpan(form, 'Email', null);
      await expectFieldColSpan(form, 'Some integer', null);
      await expectFieldColSpan(form, 'Some decimal', null);
    });

    it('customLayoutRenderer is defined by string[][], number of columns and colspan is based on template rows', async () => {
      const form = await populatePersonForm(
        1,
        {
          template: [
            ['firstName', 'lastName', 'email'],
            ['someInteger', 'someDecimal'],
            ['id', 'version'],
          ],
        },
        'screen-1440-900',
      );
      expect(form.formLayout.responsiveSteps).to.have.length(2);
      expect(form.formLayout.responsiveSteps).to.be.deep.equal([
        { minWidth: '0', columns: 1 },
        { minWidth: '800px', columns: 6 },
      ]);
      await expectFieldColSpan(form, 'First name', '2');
      await expectFieldColSpan(form, 'Last name', '2');
      await expectFieldColSpan(form, 'Email', '2');
      await expectFieldColSpan(form, 'Some integer', '3');
      await expectFieldColSpan(form, 'Some decimal', '3');
      await expectFieldColSpan(form, 'Id', '3');
      await expectFieldColSpan(form, 'Version', '3');
    });

    it('customLayoutRenderer is defined by string[][] and custom responsiveSteps, number of columns and colspan is based on responsive steps', async () => {
      const form = await populatePersonForm(
        1,
        {
          responsiveSteps: [
            { minWidth: '0', columns: 1 },
            { minWidth: '800px', columns: 2 },
            { minWidth: '1200px', columns: 3 },
          ],
          template: [['firstName', 'lastName', 'email'], ['someInteger'], ['someDecimal']],
        },
        'screen-1440-900',
      );
      expect(form.formLayout.responsiveSteps).to.have.length(3);
      expect(form.formLayout.responsiveSteps).to.be.deep.equal([
        { minWidth: '0', columns: 1 },
        { minWidth: '800px', columns: 2 },
        { minWidth: '1200px', columns: 3 },
      ]);
      await expectFieldColSpan(form, 'First name', '1');
      await expectFieldColSpan(form, 'Last name', '1');
      await expectFieldColSpan(form, 'Email', '1');
      await expectFieldColSpan(form, 'Some integer', '3');
      await expectFieldColSpan(form, 'Some decimal', '3');
    });

    it('customLayoutRenderer is defined by string[][] and custom responsiveSteps, number of columns and colspan respects the screen size', async () => {
      const form = await populatePersonForm(
        1,
        {
          responsiveSteps: [
            { minWidth: '0', columns: 1 },
            { minWidth: '800px', columns: 2 },
            { minWidth: '1200px', columns: 3 },
          ],
          template: [['firstName', 'lastName', 'email'], ['someInteger'], ['someDecimal']],
        },
        'screen-1024-768',
      );

      await expectFieldColSpan(form, 'First name', '1');
      await expectFieldColSpan(form, 'Last name', '1');
      await expectFieldColSpan(form, 'Email', '1');
      await expectFieldColSpan(form, 'Some integer', '2');
      await expectFieldColSpan(form, 'Some decimal', '2');
    });

    it('customLayoutRenderer is defined by string[][] and form is disabled, rendered fields are disabled properly', async () => {
      const form = await populatePersonForm(
        1,
        {
          responsiveSteps: [
            { minWidth: '0', columns: 1 },
            { minWidth: '800px', columns: 2 },
            { minWidth: '1200px', columns: 3 },
          ],
          template: [['firstName', 'lastName', 'email'], ['someInteger'], ['someDecimal']],
        },
        'screen-1024-768',
        true,
      );

      expect(await form.getField('First name')).to.have.attribute('disabled');
    });

    it('customLayoutRenderer is defined by FieldColSpan[][], number of columns is based on template rows and colspan is based on each FieldColSpan', async () => {
      const form = await populatePersonForm(
        1,
        {
          template: [
            [
              { property: 'firstName', colSpan: 2 },
              { property: 'lastName', colSpan: 2 },
              { property: 'email', colSpan: 2 },
            ],
            [
              { property: 'someInteger', colSpan: 3 },
              { property: 'someDecimal', colSpan: 3 },
            ],
            [
              { property: 'id', colSpan: 3 },
              { property: 'version', colSpan: 3 },
            ],
          ],
        },
        'screen-1440-900',
      );
      expect(form.formLayout.responsiveSteps).to.have.length(2);
      expect(form.formLayout.responsiveSteps).to.be.deep.equal([
        { minWidth: '0', columns: 1 },
        { minWidth: '800px', columns: 6 },
      ]);
      await expectFieldColSpan(form, 'First name', '2');
      await expectFieldColSpan(form, 'Last name', '2');
      await expectFieldColSpan(form, 'Email', '2');
      await expectFieldColSpan(form, 'Some integer', '3');
      await expectFieldColSpan(form, 'Some decimal', '3');
      await expectFieldColSpan(form, 'Id', '3');
      await expectFieldColSpan(form, 'Version', '3');
    });

    it('customLayoutRenderer is defined by FieldColSpan[][] and responsiveSteps, number of columns is based on responsiveSteps and colspan is based on each FieldColSpan', async () => {
      const form = await populatePersonForm(
        1,
        {
          responsiveSteps: [
            { minWidth: '0', columns: 1 },
            { minWidth: '800px', columns: 2 },
            { minWidth: '1200px', columns: 3 },
          ],
          template: [
            [
              { property: 'firstName', colSpan: 1 },
              { property: 'lastName', colSpan: 1 },
              { property: 'email', colSpan: 1 },
            ],
            [
              { property: 'someInteger', colSpan: 2 },
              { property: 'someDecimal', colSpan: 1 },
            ],
          ],
        },
        'screen-1440-900',
      );
      expect(form.formLayout.responsiveSteps).to.have.length(3);
      expect(form.formLayout.responsiveSteps).to.be.deep.equal([
        { minWidth: '0', columns: 1 },
        { minWidth: '800px', columns: 2 },
        { minWidth: '1200px', columns: 3 },
      ]);
      await expectFieldColSpan(form, 'First name', '1');
      await expectFieldColSpan(form, 'Last name', '1');
      await expectFieldColSpan(form, 'Email', '1');
      await expectFieldColSpan(form, 'Some integer', '2');
      await expectFieldColSpan(form, 'Some decimal', '1');
    });

    it('customLayoutRenderer is defined as a function that renders the fields in a vertical manner, VerticalLayout is used instead of FormLayout', async () => {
      function MyCustomLayoutRenderer({ children, form }: AutoFormLayoutRendererProps<PersonModel>) {
        return <VerticalLayout>{children}</VerticalLayout>;
      }
      const form = await populatePersonForm(1, MyCustomLayoutRenderer, 'screen-1440-900');
      expect(form.formLayout).to.not.exist;
      const layout = await waitFor(() => form.renderResult.querySelector('vaadin-vertical-layout')!);
      expect(layout).to.exist;
    });

    describe('AutoFormEnumField', () => {
      it('formats enum values using title case', async () => {
        const service = personService();
        const person = await getItem(service, 1);
        const result = render(<ExperimentalAutoForm service={service} model={PersonModel} item={person} />);
        const form = await FormController.init(user, result.container);
        const select = (await form.getField('Gender')) as SelectElement;

        expect(select.items).to.eql([
          {
            label: 'Male',
            value: 'MALE',
          },
          {
            label: 'Female',
            value: 'FEMALE',
          },
          {
            label: 'Non Binary',
            value: 'NON_BINARY',
          },
        ]);
      });
    });

    describe('Custom field', () => {
      it('renders a custom field instead of the default one', async () => {
        const testLabel = 'Last names';
        const testValue = 'Maxwell\nSmart';

        const service: CrudService<Person> & HasTestInfo = createService<Person>(personData);
        service.save = async (item: Person): Promise<Person | undefined> => {
          expect(item.lastName).to.equal(testValue);
          return Promise.resolve(item);
        };

        const result = await FormController.init(
          user,
          render(
            <ExperimentalAutoForm
              service={service}
              model={PersonModel}
              customFields={{
                lastName: ({ field }) => <TextArea {...field} label={testLabel} />,
              }}
            />,
          ).container,
        );

        const fields = await result.getFields(...LABELS.map((label) => (label === 'Last name' ? testLabel : label)));
        const tagNames = fields.map((field) => field.localName);
        expect(tagNames).to.eql([
          'vaadin-text-field',
          'vaadin-text-area',
          'vaadin-select',
          'vaadin-text-field',
          'vaadin-integer-field',
          'vaadin-number-field',
          'vaadin-checkbox',
          'vaadin-date-picker',
          'vaadin-time-picker',
        ]);

        await result.typeInField(testLabel, testValue);
        await result.submit();
      });
    });
  });
});
