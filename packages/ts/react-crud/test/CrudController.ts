import type { RenderResult } from '@testing-library/react';
import type userEvent from '@testing-library/user-event';
import FormController from './FormController.js';
import GridController from './GridController.js';

export class CrudController {
  readonly grid: GridController;
  readonly form: FormController;
  readonly newButton: HTMLElement;

  static async init(result: RenderResult, user: ReturnType<(typeof userEvent)['setup']>): Promise<CrudController> {
    const [grid, form] = await Promise.all([
      GridController.init(result, user),
      FormController.init(user, result.container),
    ]);
    const newButton = await result.findByText('+ New');

    return new CrudController(grid, form, newButton);
  }

  private constructor(grid: GridController, form: FormController, newButton: HTMLElement) {
    this.grid = grid;
    this.form = form;
    this.newButton = newButton;
  }
}
