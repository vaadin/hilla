import type { RenderResult } from '@testing-library/react';
import { waitFor } from '@testing-library/react';
import type userEvent from '@testing-library/user-event';
import TextFieldController from './TextFieldController.js';

type FormQueries = Pick<RenderResult, 'findByLabelText' | 'findByTestId' | 'findByText'>;

export default class FormController {
  readonly instance: HTMLElement;
  readonly #result: FormQueries;
  readonly #user: ReturnType<(typeof userEvent)['setup']>;

  static async init(result: FormQueries, user: ReturnType<(typeof userEvent)['setup']>): Promise<FormController> {
    const form = await waitFor(async () => result.findByTestId('auto-form'));

    return new FormController(form, result, user);
  }

  private constructor(instance: HTMLElement, result: FormQueries, user: ReturnType<(typeof userEvent)['setup']>) {
    this.instance = instance;
    this.#result = result;
    this.#user = user;
  }

  async getField(label: string): Promise<TextFieldController> {
    return TextFieldController.initByLabel(await this.#result.findByLabelText(label), this.#user);
  }

  async getFields(...labels: readonly string[]): Promise<readonly TextFieldController[]> {
    return await Promise.all(labels.map(async (label) => await this.getField(label)));
  }

  async findButton(label: string): Promise<HTMLButtonElement> {
    return (await this.#result.findByText(label)) as HTMLButtonElement;
  }

  async typeInField(label: string, value: string): Promise<void> {
    const field = await this.#result.findByLabelText(label);
    await this.#user.dblClick(field);
    await this.#user.keyboard(value);
  }

  async submit(): Promise<void> {
    const btn = await this.findButton('Submit');
    await this.#user.click(btn);
  }

  async discard(): Promise<void> {
    const btn = await this.findButton('Discard');
    await this.#user.click(btn);
  }

  async getValues(labels: readonly string[]): Promise<readonly unknown[]> {
    return await Promise.all(labels.map(async (label) => await this.getField(label))).then((fields) =>
      fields.map((field) => field.value),
    );
  }

  async areEnabled(labels: readonly string[]): Promise<boolean> {
    return await Promise.all(labels.map(async (label) => await this.getField(label))).then((fields) =>
      fields.every((field) => !field.disabled),
    );
  }
}
