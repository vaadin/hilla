// eslint-disable-next-line
/// <reference types="karma-viewport" />
import { expect, use } from '@esm-bundle/chai';
import { render, type RenderResult, screen, waitForElementToBeRemoved, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import chaiDom from 'chai-dom';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { type AutoCrudProps, ExperimentalAutoCrud } from '../src/autocrud.js';
import ConfirmDialogController from './ConfirmDialogController.js';
import { CrudController } from './CrudController.js';
import FormController from './FormController';
import GridController from './GridController';
import { type Person, PersonModel, personService } from './test-models-and-services.js';

use(sinonChai);
use(chaiDom);

describe('@hilla/react-crud', () => {
  describe('Auto crud', () => {
    let user: ReturnType<(typeof userEvent)['setup']>;

    beforeEach(() => {
      // Desktop resolution
      viewport.set(1024, 768);
      user = userEvent.setup();
    });

    after(() => {
      viewport.reset();
    });

    function TestAutoCrud(props: Partial<AutoCrudProps<Person>> = {}) {
      return <ExperimentalAutoCrud service={personService()} model={PersonModel} {...props} />;
    }

    it('shows a grid and a form', async () => {
      const { grid, form } = await CrudController.init(render(<TestAutoCrud />), user);
      expect(grid.instance).not.to.be.undefined;
      expect(form.instance).not.to.be.undefined;
    });

    it('edits an item when selecting a grid row', async () => {
      const { grid, form } = await CrudController.init(render(<TestAutoCrud />), user);
      await grid.toggleRowSelected(0);
      expect((await form.getField('First name')).value).to.equal('Jane');
      expect((await form.getField('Last name')).value).to.equal('Love');
    });

    it('edits an item when clicking the edit button in the actions column', async () => {
      const { grid, form } = await CrudController.init(render(<TestAutoCrud />), user);
      const cell = grid.getBodyCellContent(0, (await grid.getColumns()).length - 1);
      const editButton = await within(cell).findByRole('button', { name: 'Edit' });
      await user.click(editButton);
      expect((await form.getField('First name')).value).to.equal('Jane');
      expect((await form.getField('Last name')).value).to.equal('Love');
    });

    it('clears and disables the form when deselecting an item', async () => {
      const { grid, form } = await CrudController.init(render(<TestAutoCrud />), user);
      await grid.toggleRowSelected(1);
      await grid.toggleRowSelected(1);
      const firstName = await form.getField('First name');
      expect(firstName.value).to.equal('');
      expect(firstName.disabled).to.be.true;

      const someInteger = await form.getField('Some integer');
      expect(someInteger.value).to.equal('0');
      expect(someInteger.disabled).to.be.true;
    });

    it('disables the form when nothing is selected', async () => {
      const { form } = await CrudController.init(render(<TestAutoCrud />), user);
      const field = await form.getField('First name');
      expect(field.disabled).to.be.true;
    });

    it('refreshes the grid when the form is submitted', async () => {
      const { grid, form } = await CrudController.init(render(<TestAutoCrud />), user);
      await grid.toggleRowSelected(1);

      await form.typeInField('First name', 'foo');
      await form.submit();

      expect(grid.getBodyCellContent(1, 0)).to.have.rendered.text('foo');
    });

    it('keeps the selection when the form is submitted', async () => {
      const { grid, form } = await CrudController.init(render(<TestAutoCrud />), user);
      await grid.toggleRowSelected(1);

      await form.typeInField('First name', 'newName');
      await form.submit();

      expect(grid.isSelected(1)).to.be.true;
    });

    it('allows multiple subsequent edits', async () => {
      const { grid, form } = await CrudController.init(render(<TestAutoCrud />), user);
      await grid.toggleRowSelected(1);
      await form.typeInField('Last name', '1');
      await form.submit();
      expect(grid.getBodyCellContent(1, 1)).to.have.text('1');
      await form.typeInField('Last name', '2');
      await form.submit();
      expect(grid.getBodyCellContent(1, 1)).to.have.text('2');
    });

    it('can add a new item', async () => {
      const { grid, form, newButton } = await CrudController.init(
        render(<ExperimentalAutoCrud service={personService()} model={PersonModel} />),
        user,
      );
      await user.click(newButton);
      const [firstNameField, lastNameField, someIntegerField, someDecimalField] = await form.getFields(
        'First name',
        'Last name',
        'Some integer',
        'Some decimal',
      );

      expect(firstNameField.value).to.equal('');
      expect(lastNameField.value).to.equal('');
      expect(someIntegerField.value).to.equal('0');
      expect(someDecimalField.value).to.equal('0');
      await form.typeInField('First name', 'Jeff');
      await form.typeInField('Last name', 'Lastname');
      await form.typeInField('Email', 'first.last@domain.com');
      await form.typeInField('Some integer', '12');
      await form.typeInField('Some decimal', '12.345');
      await form.submit();
      expect(grid.getBodyCellContent(1, 0)).to.have.rendered.text('Jeff');
    });

    it('can update added item', async () => {
      const { grid, form, newButton } = await CrudController.init(
        render(<ExperimentalAutoCrud service={personService()} model={PersonModel} />),
        user,
      );
      await user.click(newButton);

      await form.typeInField('First name', 'Jeff');
      await form.typeInField('Last name', 'Lastname');
      await form.typeInField('Email', 'first.last@domain.com');
      await form.typeInField('Some integer', '12');
      await form.typeInField('Some decimal', '12.345');
      await form.submit();
      await form.typeInField('First name', 'Jerp');
      await form.submit();
      expect(grid.getBodyCellContent(1, 0)).to.have.rendered.text('Jerp');
      expect(grid.getVisibleRowCount()).to.equal(3);
    });

    it('updates grid and form when creating a new item after selecting an existing item', async () => {
      const { grid, form, newButton } = await CrudController.init(render(<TestAutoCrud />), user);
      await grid.toggleRowSelected(0);
      expect((await form.getField('First name')).value).to.equal('Jane');
      expect((await form.getField('Last name')).value).to.equal('Love');

      await user.click(newButton);

      // form should be cleared
      expect((await form.getField('First name')).value).to.equal('');
      expect((await form.getField('Last name')).value).to.equal('');

      // grid selection should reset
      expect(grid.isSelected(0)).to.be.false;
    });

    it('shows a confirmation dialog before deleting', async () => {
      const { grid } = await CrudController.init(render(<TestAutoCrud />), user);
      const cell = grid.getBodyCellContent(1, (await grid.getColumns()).length - 1);
      const deleteButton = await within(cell).findByRole('button', { name: 'Delete' });
      await user.click(deleteButton);
      const dialog = await ConfirmDialogController.init(document.body, user);
      expect(dialog.text).to.equal('Are you sure you want to delete the selected item?');
      expect(grid.getBodyCellContent(1, 1)).to.have.rendered.text('Dove');
    });

    it('deletes and refreshes grid after confirming', async () => {
      const { grid } = await CrudController.init(render(<TestAutoCrud />), user);
      expect(grid.getVisibleRowCount()).to.equal(2);
      const cell = grid.getBodyCellContent(1, (await grid.getColumns()).length - 1);
      const deleteButton = await within(cell).findByRole('button', { name: 'Delete' });
      await user.click(deleteButton);
      const dialog = await ConfirmDialogController.init(document.body, user);
      await dialog.confirm();
      expect(grid.getVisibleRowCount()).to.equal(1);
    });

    it('clears and disables the form when deleting the currently edited item', async () => {
      const { grid, form } = await CrudController.init(render(<TestAutoCrud />), user);
      // Select item
      await grid.toggleRowSelected(0);
      // Delete item
      const cell = grid.getBodyCellContent(0, (await grid.getColumns()).length - 1);
      const deleteButton = await within(cell).findByRole('button', { name: 'Delete' });
      await user.click(deleteButton);
      const dialog = await ConfirmDialogController.init(document.body, user);
      await dialog.confirm();
      // Form should be cleared and disabled
      const field = await form.getField('First name');
      expect(field.disabled).to.be.true;
      expect(field.value).to.be.empty;
    });

    it('does not delete when not confirming', async () => {
      const { grid } = await CrudController.init(render(<TestAutoCrud />), user);
      expect(grid.getVisibleRowCount()).to.equal(2);
      const cell = grid.getBodyCellContent(1, (await grid.getColumns()).length - 1);
      const deleteButton = await within(cell).findByRole('button', { name: 'Delete' });
      await user.click(deleteButton);
      const dialog = await ConfirmDialogController.init(document.body, user);
      await dialog.cancel();
      expect(grid.getVisibleRowCount()).to.equal(2);
    });

    it('does render a delete button without noDelete', async () => {
      const { grid } = await CrudController.init(render(<TestAutoCrud />), user);
      const cell = grid.getBodyCellContent(1, (await grid.getColumns()).length - 1);
      const deleteButton = within(cell).queryByRole('button', { name: 'Delete' });
      expect(deleteButton).to.exist;
    });

    it('does not render a delete button with noDelete', async () => {
      const { grid } = await CrudController.init(render(<TestAutoCrud noDelete />), user);
      const cell = grid.getBodyCellContent(1, (await grid.getColumns()).length - 1);
      const deleteButton = within(cell).queryByRole('button', { name: 'Delete' });
      expect(deleteButton).to.be.null;
    });

    describe('mobile layout', () => {
      let saveSpy: sinon.SinonSpy;
      let result: RenderResult;

      beforeEach(() => {
        // iPhone 13 Pro resolution
        viewport.set(390, 844);

        const service = personService();
        saveSpy = sinon.spy(service, 'save');
        result = render(<TestAutoCrud service={service} />);
      });

      afterEach(() => {
        // cleanup dangling overlay
        const overlay = document.querySelector('vaadin-dialog-overlay');
        if (overlay) {
          overlay.remove();
        }
      });

      it('opens the form in a dialog when selecting an item', async () => {
        const grid = await GridController.init(result, user);
        await grid.toggleRowSelected(0);

        const form = await FormController.init(user);
        expect(form.instance).to.exist;
        expect((await form.getField('First name')).value).to.equal('Jane');
        expect((await form.getField('Last name')).value).to.equal('Love');
      });

      it('opens the form in a dialog when creating a new item', async () => {
        const newButton = await result.findByText('+ New');
        await user.click(newButton);

        const form = await FormController.init(user);
        expect(form.instance).to.exist;
        expect((await form.getField('First name')).value).to.equal('');
        expect((await form.getField('Last name')).value).to.equal('');
      });

      it('closes the dialog when clicking close button', async () => {
        const grid = await GridController.init(result, user);
        await grid.toggleRowSelected(0);

        const dialogOverlay = await screen.findByRole('dialog');
        const closeButton = await within(dialogOverlay).findByRole('button', { name: 'Close' });
        await user.click(closeButton);

        // dialog is closed
        await waitForElementToBeRemoved(() => screen.queryByRole('dialog'));

        // grid selection is cleared
        expect(grid.isSelected(0)).to.be.false;

        // does not save
        expect(saveSpy).not.to.have.been.called;
      });

      it('closes the dialog when clicking submit button', async () => {
        const grid = await GridController.init(result, user);
        await grid.toggleRowSelected(0);

        const form = await FormController.init(user);
        await form.typeInField('First name', 'J'); // to enable the submit button
        await form.submit();

        // dialog is closed
        await waitForElementToBeRemoved(() => screen.queryByRole('dialog'));

        // grid selection is cleared
        expect(grid.isSelected(0)).to.be.false;

        // saves
        expect(saveSpy).to.have.been.called;
      });
    });

    describe('customize grid', () => {
      it('allows passing custom grid props', async () => {
        const { grid } = await CrudController.init(
          render(
            <ExperimentalAutoCrud
              service={personService()}
              model={PersonModel}
              gridProps={{ visibleColumns: ['firstName', 'lastName'] }}
            />,
          ),
          user,
        );

        await expect(grid.getHeaderCellContents()).to.eventually.eql(['First name', 'Last name', '']);
      });
    });

    describe('customize form', () => {
      it('allows passing custom form props', async () => {
        const { form } = await CrudController.init(
          render(
            <ExperimentalAutoCrud
              service={personService()}
              model={PersonModel}
              formProps={{ customLayoutRenderer: { template: [['firstName', 'lastName']] } }}
            />,
          ),
          user,
        );

        expect(form.queryField('First name')).to.exist;
        expect(form.queryField('Last name')).to.exist;
        expect(form.queryField('Email')).not.to.exist;
      });
    });

    it('renders with custom id, class name and style property on top most element', () => {
      const { container } = render(
        <ExperimentalAutoCrud
          service={personService()}
          model={PersonModel}
          id="my-id"
          className="custom-auto-crud"
          style={{ backgroundColor: 'blue' }}
        />,
      );

      const autoCrudElement = container.firstElementChild as HTMLElement;

      expect(autoCrudElement).to.exist;
      expect(autoCrudElement.id).to.equal('my-id');
      expect(autoCrudElement.style.backgroundColor).to.equal('blue');
      expect(autoCrudElement.className).to.include('custom-auto-crud');
    });
  });
});
