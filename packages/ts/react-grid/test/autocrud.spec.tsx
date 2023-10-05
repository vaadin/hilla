import { expect, use } from '@esm-bundle/chai';
import type { GridElement } from '@hilla/react-components/Grid.js';
import { render, type RenderResult } from '@testing-library/react';
import sinonChai from 'sinon-chai';
import { ExperimentalAutoCrud, type AutoCrudProps } from '../src/autocrud.js';
import { getFormField, setFormField, submit } from './form-test-utils.js';
import {
  getBodyCellContent,
  getGrid,
  getVisibleRowCount,
  isSelected,
  reactRender,
  toggleRowSelected,
} from './grid-test-helpers.js';
import { findConfirmDialog } from './test-confirm-dialog.js';
import { PersonModel, personService, type Person } from './test-models-and-services.js';

use(sinonChai);

function getForm(result: RenderResult): HTMLElement {
  return getGrid(result).nextElementSibling as HTMLElement;
}

describe('@hilla/react-grid', () => {
  function TestAutoCrud(customProps: Partial<AutoCrudProps<Person>>) {
    return <ExperimentalAutoCrud service={personService()} model={PersonModel} {...customProps} />;
  }
  describe('Auto crud', () => {
    it('shows a grid and a form', () => {
      const result = render(<TestAutoCrud />);
      const grid = getGrid(result);
      const form = getForm(result);
      expect(grid).not.to.be.undefined;
      expect(form).not.to.be.undefined;
    });
    it('passes the selected item and populates the form', async () => {
      const result = render(<TestAutoCrud />);
      await reactRender();
      const grid = getGrid(result);
      await reactRender();
      toggleRowSelected(grid, 0);
      await reactRender();
      expect((await getFormField(result, 'First name')).value).to.equal('Jane');
      expect((await getFormField(result, 'Last name')).value).to.equal('Love');
    });
    it('clears and disables the form when deselecting an item', async () => {
      const result = render(<TestAutoCrud />);
      const grid = getGrid(result);
      await reactRender();
      toggleRowSelected(grid, 1);
      await reactRender();
      toggleRowSelected(grid, 1);
      await reactRender();
      const firstName = await getFormField(result, 'First name');
      expect(firstName.value).to.equal('');
      expect(firstName.disabled).to.be.true;

      const someNumber = await getFormField(result, 'Some number');
      expect(someNumber.value).to.equal(0);
      expect(someNumber.disabled).to.be.true;
    });
    it('disables the form when nothing is selected', async () => {
      const result = render(<TestAutoCrud />);
      const field = await getFormField(result, 'First name');
      expect(field.disabled).to.be.true;
    });
    it('refreshes the grid when the form is submitted', async () => {
      const service = personService();
      const result = render(<ExperimentalAutoCrud service={service} model={PersonModel} />);
      const grid: GridElement<Person> = getGrid(result);
      await reactRender();
      toggleRowSelected(grid, 1);
      await reactRender();
      await setFormField(result, 'First name', 'foo');
      await reactRender();
      await submit(result);
      await reactRender();
      expect(getBodyCellContent(grid, 1, 0).innerText).to.equal('foo');
    });
    it('keeps the selection when the form is submitted', async () => {
      const service = personService();
      const result = render(<ExperimentalAutoCrud service={service} model={PersonModel} />);
      const grid: GridElement<Person> = getGrid(result);
      await reactRender();
      toggleRowSelected(grid, 1);
      await reactRender();
      await setFormField(result, 'First name', 'newName');
      await reactRender();
      await submit(result);
      await reactRender();
      expect(isSelected(grid, 1)).to.be.true;
    });
    it('allows multiple subsequent edits', async () => {
      const service = personService();
      const result = render(<ExperimentalAutoCrud service={service} model={PersonModel} />);
      const grid: GridElement<Person> = getGrid(result);
      await reactRender();
      toggleRowSelected(grid, 1);
      await reactRender();
      await setFormField(result, 'Last name', '1');
      await reactRender();
      await submit(result);
      await reactRender();
      expect(getBodyCellContent(grid, 1, 1).innerText).to.equal('1');
      await setFormField(result, 'Last name', '2');
      await reactRender();
      await submit(result);
      await reactRender();
      expect(getBodyCellContent(grid, 1, 1).innerText).to.equal('2');
    });
    it('shows a confirmation dialog before deleting', async () => {
      const service = personService();
      const result = render(<ExperimentalAutoCrud service={service} model={PersonModel} />);
      const grid: GridElement<Person> = getGrid(result);
      await reactRender();
      const cell = getBodyCellContent(grid, 1, 5);
      (cell.querySelector('vaadin-button') as HTMLElement).click();
      await reactRender();
      const dialog = findConfirmDialog(cell);
      expect(dialog?._getDialogText()).to.equal('Are you sure you want to delete the selected item?');
      expect('Dove').to.equal(getBodyCellContent(grid, 1, 1).innerText);
    });
    it('can add a new item', async () => {
      const service = personService();
      const result = render(<ExperimentalAutoCrud service={service} model={PersonModel} />);
      const grid: GridElement<Person> = getGrid(result);
      await reactRender();
      const newButton = await result.findByText('+ New');
      newButton.click();
      await reactRender();
      expect((await getFormField(result, 'First name')).value).to.equal('');
      expect((await getFormField(result, 'Last name')).value).to.equal('');
      expect((await getFormField(result, 'Some number')).value).to.equal(0);
      await setFormField(result, 'First name', 'Jeff');
      await setFormField(result, 'Last name', 'Lastname');
      await setFormField(result, 'Email', 'first.last@domain.com');
      await setFormField(result, 'Some number', '12');
      await reactRender();
      await submit(result);
      await reactRender();
      expect(getBodyCellContent(grid, 1, 0).innerText).to.equal('Jeff');
    });
    it('can update added item', async () => {
      const service = personService();
      const result = render(<ExperimentalAutoCrud service={service} model={PersonModel} />);
      const grid: GridElement<Person> = getGrid(result);
      await reactRender();
      const newButton = await result.findByText('+ New');
      newButton.click();
      await reactRender();
      await setFormField(result, 'First name', 'Jeff');
      await setFormField(result, 'Last name', 'Lastname');
      await setFormField(result, 'Email', 'first.last@domain.com');
      await setFormField(result, 'Some number', '12');
      await submit(result);
      await reactRender();
      await setFormField(result, 'First name', 'Jerp');
      await submit(result);
      await reactRender();
      expect(getBodyCellContent(grid, 1, 0).innerText).to.equal('Jerp');
      expect(getVisibleRowCount(grid)).to.equal(3);
    });
    it('deletes and refreshes grid after confirming', async () => {
      const service = personService();
      const result = render(<ExperimentalAutoCrud service={service} model={PersonModel} />);
      const grid: GridElement<Person> = getGrid(result);
      await reactRender();
      expect(getVisibleRowCount(grid)).to.equal(2);
      const cell = getBodyCellContent(grid, 1, 5);
      (cell.querySelector('vaadin-button') as HTMLElement).click();
      await reactRender();
      const dialog = findConfirmDialog(cell)!;
      dialog._getConfirmButton().click();
      await reactRender();
      expect(getVisibleRowCount(grid)).to.equal(1);
    });
    it('does not delete when not confirming', async () => {
      const service = personService();
      const result = render(<ExperimentalAutoCrud service={service} model={PersonModel} />);
      const grid: GridElement<Person> = getGrid(result);
      await reactRender();
      expect(getVisibleRowCount(grid)).to.equal(2);
      const cell = getBodyCellContent(grid, 1, 5);
      (cell.querySelector('vaadin-button') as HTMLElement).click();
      await reactRender();
      const dialog = findConfirmDialog(cell)!;
      dialog._getCancelButton().click();
      await reactRender();
      expect(getVisibleRowCount(grid)).to.equal(2);
    });
    it('does not render a delete button when noDelete', async () => {
      const service = personService();
      const result = render(<ExperimentalAutoCrud service={service} model={PersonModel} noDelete />);
      const grid: GridElement<Person> = getGrid(result);
      await reactRender();
      const cell = getBodyCellContent(grid, 1, 5);
      expect(cell).to.be.null;
    });
  });
});
