import { expect, use } from '@esm-bundle/chai';
import type { GridElement } from '@hilla/react-components/Grid.js';
import { render, type RenderResult } from '@testing-library/react';
import sinonChai from 'sinon-chai';
import { ExperimentalAutoCrud, type AutoCrudProps } from '../src/autocrud.js';
import { getFormField, setFormField, submit } from './form-test-utils.js';
import { getGrid, nextFrame, toggleRowSelected } from './grid-test-helpers.js';
import { PersonModel, createService, personData, personService, type Person } from './test-models-and-services.js';

use(sinonChai);

function getForm(result: RenderResult): HTMLElement {
  return getGrid(result).nextElementSibling as HTMLElement;
}

describe('@hilla/react-grid', () => {
  function TestAutoCrud(customProps: Partial<AutoCrudProps<Person>>) {
    return <ExperimentalAutoCrud service={personService()} model={PersonModel} {...customProps} />;
  }
  describe('Auto crud', () => {
    it('shows a grid and a form', async () => {
      const result = render(<TestAutoCrud />);
      const grid = getGrid(result);
      const form = getForm(result);
      expect(grid).not.to.be.undefined;
      expect(form).not.to.be.undefined;
    });
    it('passes the selected item and populates the form', async () => {
      const result = render(<TestAutoCrud />);
      const grid = getGrid(result);
      await nextFrame();
      await nextFrame();
      toggleRowSelected(grid, 1);
      await nextFrame();
      expect((await getFormField(result, 'First name')).value).to.equal('Jane');
      expect((await getFormField(result, 'Last name')).value).to.equal('Love');
    });
    it('clears the form when deselecting an item', async () => {
      const result = render(<TestAutoCrud />);
      const grid = getGrid(result);
      await nextFrame();
      await nextFrame();
      toggleRowSelected(grid, 1);
      await nextFrame();
      toggleRowSelected(grid, 1);
      expect((await getFormField(result, 'First name')).value).to.equal('');
      expect((await getFormField(result, 'Last name')).value).to.equal('');
    });
    it('disables the form when nothing is selected', async () => {
      const result = render(<TestAutoCrud />);
      const field = await getFormField(result, 'First name');
      expect(field.disabled).to.be.true;
    });
    it('refreshes the grid when the form is submitted', async () => {
      const service = createService(personData);
      const result = render(<ExperimentalAutoCrud service={service} model={PersonModel} />);
      const grid: GridElement<Person> = getGrid(result);
      await nextFrame();
      toggleRowSelected(grid, 1);
      await nextFrame();
      await setFormField(result, 'First name', 'foo');
      await nextFrame();
      await submit(result);
      await nextFrame();
      toggleRowSelected(grid, 1);
      expect(grid.activeItem!.firstName).to.equal('foo');
    });
  });
});
