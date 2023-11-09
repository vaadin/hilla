// eslint-disable-next-line
/// <reference types="karma-viewport" />

import { expect, use } from '@esm-bundle/chai';
import type { SelectElement } from '@hilla/react-components/Select.js';
import { TextArea } from '@hilla/react-components/TextArea.js';
import { VerticalLayout } from '@hilla/react-components/VerticalLayout.js';
import { render, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import chaiAsPromised from 'chai-as-promised';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { type AutoFormLayoutRendererProps, type AutoFormProps, emptyItem, AutoForm } from '../src/autoform.js';
import type { CrudService } from '../src/crud.js';
import ConfirmDialogController from './ConfirmDialogController';
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
      formProps?: Omit<AutoFormProps<PersonModel>, 'item' | 'model' | 'service'>,
      screenSize?: string,
      disabled?: boolean,
    ): Promise<FormController> {
      if (screenSize) {
        viewport.set(screenSize);
      }
      const service = personService();
      const person = await getItem(service, personId);
      const result = render(
        <AutoForm service={service} model={PersonModel} item={person} disabled={disabled} {...formProps} />,
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
        render(<AutoForm service={personService()} model={PersonModel} item={person} />).container,
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
        render(<AutoForm service={personService()} model={PersonModel} />).container,
      );
      await expect(form.getValues(...LABELS)).to.eventually.be.deep.equal(getExpectedValues(DEFAULT_PERSON));
    });

    it('uses values from an item', async () => {
      const service = personService();
      const person = (await getItem(service, 2))!;

      const form = await FormController.init(
        user,
        render(<AutoForm service={service} model={PersonModel} item={person} />).container,
      );
      await expect(form.getValues(...LABELS)).to.eventually.be.deep.equal(getExpectedValues(person));
    });

    it('updates values when changing item', async () => {
      const service = personService();
      const person1 = (await getItem(service, 2))!;
      const person2 = (await getItem(service, 1))!;

      const result = render(<AutoForm service={service} model={PersonModel} item={person1} />);
      let form = await FormController.init(user, result.container);
      await expect(form.getValues(...LABELS)).to.eventually.be.deep.equal(getExpectedValues(person1));

      result.rerender(<AutoForm service={service} model={PersonModel} item={person2} />);
      form = await FormController.init(user, result.container);
      await expect(form.getValues(...LABELS)).to.eventually.be.deep.equal(getExpectedValues(person2));
    });

    it('clears the form when setting the item to undefined', async () => {
      const service = personService();
      const person = (await getItem(service, 2))!;

      const result = render(<AutoForm service={service} model={PersonModel} item={person} />);
      let form = await FormController.init(user, result.container);
      await expect(form.getValues(...LABELS)).to.eventually.be.deep.equal(getExpectedValues(person));

      result.rerender(<AutoForm service={service} model={PersonModel} item={undefined} />);
      form = await FormController.init(user, result.container);
      await expect(form.getValues(...LABELS)).to.eventually.be.deep.equal(getExpectedValues(DEFAULT_PERSON));
    });

    it('submits a valid form', async () => {
      const service: CrudService<Person> & HasTestInfo = createService<Person>(personData);
      const saveSpy = sinon.spy(service, 'save');

      const form = await FormController.init(
        user,
        render(<AutoForm service={service} model={PersonModel} item={undefined} />).container,
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
        render(<AutoForm service={service} model={PersonModel} item={person} />).container,
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
        render(<AutoForm service={service} model={PersonModel} item={person} afterSubmit={submitSpy} />).container,
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
        render(<AutoForm service={service} model={PersonModel} item={person} afterSubmit={submitSpy} />).container,
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

      const result = render(<AutoForm service={service} model={PersonModel} item={person} afterSubmit={submitSpy} />);
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
        <AutoForm
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
        render(<AutoForm service={personService()} model={PersonModel} disabled />).container,
      );
      await expect(form.areEnabled(...LABELS)).to.eventually.be.false;
    });

    it('enables all fields and buttons when enabled', async () => {
      const service = personService();
      const result = render(<AutoForm service={service} model={PersonModel} disabled />);
      await FormController.init(user, result.container);
      result.rerender(<AutoForm service={service} model={PersonModel} />);
      const form = await FormController.init(user, result.container);
      await expect(form.areEnabled(...LABELS)).to.eventually.be.true;
    });

    describe('discard button', () => {
      it('does not show a discard button if the form is not dirty', async () => {
        const form = await FormController.init(
          user,
          render(<AutoForm service={personService()} model={PersonModel} />).container,
        );
        await expect(form.findButton('Discard')).to.eventually.be.rejected;
      });

      it('does show a discard button if the form is dirty', async () => {
        const form = await FormController.init(
          user,
          render(<AutoForm service={personService()} model={PersonModel} />).container,
        );
        await form.typeInField('First name', 'foo');
        await expect(form.findButton('Discard')).to.eventually.exist;
      });

      it('resets the form when clicking the discard button', async () => {
        const form = await FormController.init(
          user,
          render(<AutoForm service={personService()} model={PersonModel} />).container,
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
      const result = render(<AutoForm service={service} model={PersonModel} />);
      const form = await FormController.init(user, result.container);

      const submitButton = await form.findButton('Submit');
      expect(submitButton.disabled).to.be.false;
    });

    it('passing null interprets as creating new, submit button is enabled at the beginning', async () => {
      const service = personService();
      const result = render(<AutoForm service={service} model={PersonModel} item={null} />);
      const form = await FormController.init(user, result.container);

      const submitButton = await form.findButton('Submit');
      expect(submitButton.disabled).to.be.false;
    });

    it('passing undefined interprets as creating new, submit button is enabled at the beginning', async () => {
      const service = personService();
      const result = render(<AutoForm service={service} model={PersonModel} item={undefined} />);
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

    describe('layoutRenderer', () => {
      it('uses default two-column form layout if layoutRenderer is not defined', async () => {
        const form = await populatePersonForm(1);
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

      it('uses layoutRenderer if defined, instead of the default FormLayout', async () => {
        function MyLayoutRenderer({ children }: AutoFormLayoutRendererProps<PersonModel>) {
          return <VerticalLayout>{children}</VerticalLayout>;
        }

        const form = await populatePersonForm(1, { layoutRenderer: MyLayoutRenderer }, 'screen-1440-900');
        expect(form.formLayout).to.not.exist;
        const layout = await waitFor(() => form.renderResult.querySelector('vaadin-vertical-layout')!);
        expect(layout).to.exist;
      });
    });

    describe('visibleFields', () => {
      it('renders the form according to visibleFields if defined', async () => {
        const form = await populatePersonForm(1, { visibleFields: ['firstName', 'lastName', 'id', 'dummy'] });
        const fields = await form.getFields('First name', 'Last name', 'Id');
        const tagNames = fields.map((field) => field.localName);
        expect(tagNames).to.eql(['vaadin-text-field', 'vaadin-text-field', 'vaadin-number-field']);
        expect(form.queryField('Dummy')).to.be.undefined;
        const genderField = form.queryField('Gender');
        expect(genderField).to.be.undefined;
      });
    });

    describe('Delete button', () => {
      let service: CrudService<Person> & HasTestInfo;
      let person: Person;
      let deleteStub: sinon.SinonStub;
      let afterDeleteSpy: sinon.SinonSpy;
      let onDeleteErrorSpy: sinon.SinonSpy;

      beforeEach(async () => {
        service = personService();
        person = (await getItem(service, 2))!;
        deleteStub = sinon.stub(service, 'delete');
        deleteStub.returns(Promise.resolve());
        afterDeleteSpy = sinon.spy();
        onDeleteErrorSpy = sinon.spy();
      });

      afterEach(() => {
        // cleanup dangling overlay
        const overlay = document.querySelector('vaadin-confirm-dialog-overlay');
        if (overlay) {
          overlay.remove();
        }
      });

      async function renderForm(item: Person | typeof emptyItem | null, enableDelete: boolean) {
        return FormController.init(
          user,
          render(
            <AutoForm
              service={service}
              model={PersonModel}
              item={item}
              deleteButtonVisible={enableDelete}
              afterDelete={afterDeleteSpy}
              onDeleteError={onDeleteErrorSpy}
            />,
          ).container,
        );
      }

      it('does not show a delete button by default', async () => {
        const form = await renderForm(person, false);

        expect(form.queryButton('Delete...')).to.not.exist;
      });

      it('does show a delete button if delete button is enabled', async () => {
        const form = await renderForm(person, true);

        expect(form.queryButton('Delete...')).to.exist;
      });

      it('does not show a delete button if nothing is edited', async () => {
        const form = await renderForm(null, true);

        expect(form.queryButton('Delete...')).to.not.exist;
      });

      it('does not show a delete button if a new item is edited', async () => {
        const form = await renderForm(emptyItem, true);

        expect(form.queryButton('Delete...')).to.not.exist;
      });

      it('does shows confirmation dialog before deleting', async () => {
        const form = await renderForm(person, true);
        const deleteButton = await form.findButton('Delete...');
        await userEvent.click(deleteButton);

        const dialog = await ConfirmDialogController.init(document.body, user);
        expect(dialog.text).to.equal('Are you sure you want to delete the selected item?');
      });

      it('deletes item and calls success callback after confirming', async () => {
        const form = await renderForm(person, true);
        const deleteButton = await form.findButton('Delete...');
        await userEvent.click(deleteButton);

        const dialog = await ConfirmDialogController.init(document.body, user);
        await dialog.confirm();

        expect(deleteStub).to.have.been.calledOnce;
        expect(afterDeleteSpy).to.have.been.calledOnce;
        expect(onDeleteErrorSpy).to.not.have.been.called;
      });

      it('does not delete item nor call callbacks after canceling', async () => {
        const form = await renderForm(person, true);
        const deleteButton = await form.findButton('Delete...');
        await userEvent.click(deleteButton);

        const dialog = await ConfirmDialogController.init(document.body, user);
        await dialog.cancel();

        expect(deleteStub).to.not.have.been.called;
        expect(afterDeleteSpy).to.not.have.been.called;
        expect(onDeleteErrorSpy).to.not.have.been.called;
      });

      it('calls error callback if delete fails', async () => {
        const error = new Error('Delete failed');
        deleteStub.returns(Promise.reject(error));

        const form = await renderForm(person, true);
        const deleteButton = await form.findButton('Delete...');
        await userEvent.click(deleteButton);

        const dialog = await ConfirmDialogController.init(document.body, user);
        await dialog.confirm();

        expect(deleteStub).to.have.been.calledOnce;
        expect(afterDeleteSpy).to.not.have.been.called;
        expect(onDeleteErrorSpy).to.have.been.calledOnce;
        expect(onDeleteErrorSpy).to.have.been.calledWith(sinon.match.hasNested('error.message', 'Delete failed'));
      });
    });

    describe('AutoFormEnumField', () => {
      it('formats enum values using title case', async () => {
        const service = personService();
        const person = await getItem(service, 1);
        const result = render(<AutoForm service={service} model={PersonModel} item={person} />);
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

    describe('Field Options', () => {
      it('renders custom field from field options instead of the default one', async () => {
        const service = personService();
        const saveSpy = sinon.spy(service, 'save');

        const result = await FormController.init(
          user,
          render(
            <AutoForm
              service={service}
              model={PersonModel}
              fieldOptions={{
                lastName: {
                  label: 'Custom last name',
                  renderer: ({ field }) => <TextArea key={field.name} {...field} />,
                },
              }}
            />,
          ).container,
        );

        const field = await result.getField('Custom last name');
        expect(field.localName).to.equal('vaadin-text-area');

        await result.typeInField('Custom last name', 'Maxwell\nSmart');
        await result.submit();

        expect(saveSpy).to.have.been.calledOnce;
        expect(saveSpy).to.have.been.calledWith(sinon.match.hasNested('lastName', 'Maxwell\nSmart'));
      });

      it('disables custom field from field options when form is disabled', async () => {
        const result = await FormController.init(
          user,
          render(
            <AutoForm
              service={personService()}
              model={PersonModel}
              disabled
              fieldOptions={{
                lastName: {
                  renderer: ({ field }) => <TextArea key={field.name} {...field} />,
                },
              }}
            />,
          ).container,
        );

        const field = await result.getField('Last name');
        expect(field.disabled).to.be.true;
      });

      it('allows setting a custom label on a custom field from field options', async () => {
        const result = await FormController.init(
          user,
          render(
            <AutoForm
              service={personService()}
              model={PersonModel}
              disabled
              fieldOptions={{
                lastName: {
                  label: 'This should not be used',
                  renderer: ({ field }) => <TextArea key={field.name} {...field} label="Custom last name" />,
                },
              }}
            />,
          ).container,
        );

        const field = result.queryField('Custom last name');
        expect(field).to.exist;
      });

      it('renders custom label from field options instead of the default one', () => {
        const result = render(
          <AutoForm
            service={personService()}
            model={PersonModel}
            fieldOptions={{
              firstName: { label: 'Employee First Name' },
              lastName: { label: 'Employee Last Name' },
            }}
          />,
        );

        expect(within(result.container).queryByLabelText('Employee First Name')).to.exist;
        expect(within(result.container).queryByLabelText('First name')).to.not.exist;
        expect(within(result.container).queryByLabelText('Employee Last Name')).to.exist;
        expect(within(result.container).queryByLabelText('Last name')).to.not.exist;
      });

      it('passes colspan to fields', async () => {
        const result = await FormController.init(
          user,
          render(
            <AutoForm
              service={personService()}
              model={PersonModel}
              fieldOptions={{
                firstName: { colspan: 2 },
                lastName: { colspan: 3 },
                email: { colspan: 4 },
                someInteger: { colspan: 5 },
                someDecimal: { colspan: 6 },
              }}
            />,
          ).container,
        );

        await expectFieldColSpan(result, 'First name', '2');
        await expectFieldColSpan(result, 'Last name', '3');
        await expectFieldColSpan(result, 'Email', '4');
        await expectFieldColSpan(result, 'Some integer', '5');
        await expectFieldColSpan(result, 'Some decimal', '6');
      });
    });

    describe('formLayoutProps', () => {
      it('passes responsiveSteps to FormLayout', async () => {
        const form = await populatePersonForm(1, {
          formLayoutProps: {
            responsiveSteps: [{ minWidth: 0, columns: 1, labelsPosition: 'top' }],
          },
        });
        expect(form.formLayout.responsiveSteps).to.have.length(1);
        expect(form.formLayout.responsiveSteps).to.be.deep.equal([{ minWidth: 0, columns: 1, labelsPosition: 'top' }]);
      });
    });

    describe('customize style props', () => {
      it('renders properly without custom id, class name and style property', () => {
        const { container } = render(<AutoForm service={personService()} model={PersonModel} />);
        const autoFormElement = container.firstElementChild as HTMLElement;

        expect(autoFormElement).to.exist;
        expect(autoFormElement.id).to.equal('');
        expect(autoFormElement.className.trim()).to.equal('auto-form');
        expect(autoFormElement.getAttribute('style')).to.equal(null);
      });

      it('renders with custom id, class name and style property on top most element', () => {
        const { container } = render(
          <AutoForm
            service={personService()}
            model={PersonModel}
            id="my-id"
            className="custom-auto-form"
            style={{ backgroundColor: 'blue' }}
          />,
        );
        const autoFormElement = container.firstElementChild as HTMLElement;

        expect(autoFormElement).to.exist;
        expect(autoFormElement.id).to.equal('my-id');
        expect(autoFormElement.className.trim()).to.equal('auto-form custom-auto-form');
        expect(autoFormElement.getAttribute('style')).to.equal('background-color: blue;');
      });
    });
  });
});
