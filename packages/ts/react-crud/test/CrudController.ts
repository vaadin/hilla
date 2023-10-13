import type { RenderResult } from '@testing-library/react';
import type userEvent from '@testing-library/user-event';
import FormController from './FormController.js';
import GridController from './GridController.js';

export class CrudController {
  readonly grid: GridController;
  readonly form: FormController;

  static async init(result: RenderResult, user: ReturnType<(typeof userEvent)['setup']>): Promise<CrudController> {
    const [grid, form] = await Promise.all([GridController.init(result, user), FormController.init(result, user)]);

    return new CrudController(grid, form);
  }

  private constructor(grid: GridController, form: FormController) {
    this.grid = grid;
    this.form = form;
  }
}
