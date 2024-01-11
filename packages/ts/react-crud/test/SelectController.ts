import { waitFor } from '@testing-library/react';
import type userEvent from '@testing-library/user-event';
import { SelectElement } from '@vaadin/react-components/Select.js';

export default class SelectController {
  readonly instance: SelectElement;
  readonly #user: ReturnType<(typeof userEvent)['setup']>;

  static async init(base: HTMLElement, user: ReturnType<(typeof userEvent)['setup']>): Promise<SelectController> {
    const instance = base instanceof SelectElement ? base : await waitFor(() => base.querySelector('vaadin-select')!);
    return new SelectController(instance, user);
  }

  private constructor(instance: SelectElement, user: ReturnType<(typeof userEvent)['setup']>) {
    this.instance = instance;
    this.#user = user;
  }

  async select(option: string): Promise<void> {
    const btn = await waitFor(() => this.instance.querySelector('[role="button"]')!);
    await this.#user.click(btn);
    const opts = await waitFor(() => document.querySelector('[role="listbox"]')!);
    await this.#user.selectOptions(opts, option);
  }
}
