import { type RenderResult, waitFor } from '@testing-library/react';
import type userEvent from '@testing-library/user-event';

export class FormFieldController {
  readonly #result: RenderResult;
  readonly #user: ReturnType<(typeof userEvent)['setup']>;

  constructor(result: RenderResult, user: ReturnType<(typeof userEvent)['setup']>) {
    this.#result = result;
    this.#user = user;
  }

  async select(elementTestId: string, optionToSelect: string): Promise<void> {
    const field = await this.#result.findByTestId(elementTestId);
    const btn = await waitFor(() => field.querySelector('[role="button"]')!);
    await this.#user.click(btn);
    const opts = await waitFor(() => document.querySelector('[role="listbox"]')!);
    await this.#user.selectOptions(opts, optionToSelect);
  }

  async typeInTextField(elementTestId: string, value: string): Promise<void> {
    const field = await this.#result.findByTestId(elementTestId);
    const input = field.querySelector('input')!;
    await this.#user.dblClick(input);
    await this.#user.keyboard(value);
  }
}
