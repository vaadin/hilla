import type { RenderResult } from '@testing-library/react';
import { waitFor } from '@testing-library/react';
import type userEvent from '@testing-library/user-event';
import { FormFieldController } from './FormFieldController.js';

export type FormElement = HTMLElement & {
  value: unknown;
  disabled: boolean;
};

export default class FormController {
  readonly instance: HTMLElement;
  readonly #result: RenderResult;
  readonly #user: ReturnType<(typeof userEvent)['setup']>;

  static async init(result: RenderResult, user: ReturnType<(typeof userEvent)['setup']>): Promise<FormController> {
    const form = (await waitFor(
      () => result.container.querySelector('vertical-layout[theme="padding"]')!,
    )) as HTMLElement;
    return new FormController(form, result, user);
  }

  private constructor(form: HTMLElement, result: RenderResult, user: ReturnType<(typeof userEvent)['setup']>) {
    this.instance = form;
    this.#result = result;
    this.#user = user;
  }

  getFieldController(): FormFieldController {
    return new FormFieldController(this.#result, this.#user);
  }

  async getField(testId: string): Promise<FormElement> {
    return (await this.#result.findByTestId(testId)) as FormElement;
  }

  async getFields(...testIds: readonly string[]): Promise<readonly FormElement[]> {
    return await Promise.all(testIds.map(async (testId) => await this.getField(testId)));
  }

  async findButton(text: string): Promise<HTMLButtonElement> {
    return (await this.#result.findByText(text)) as HTMLButtonElement;
  }

  async submit(): Promise<void> {
    const btn = await this.findButton('Submit');
    await this.#user.click(btn);
  }

  async discard(): Promise<void> {
    const btn = await this.findButton('Discard');
    await this.#user.click(btn);
  }

  async getValues(...labels: readonly string[]): Promise<readonly unknown[]> {
    return await Promise.all(labels.map(async (label) => await this.getField(label))).then((fields) =>
      fields.map((field) => field.value),
    );
  }

  async areEnabled(...labels: readonly string[]): Promise<boolean> {
    return await Promise.all(labels.map(async (label) => await this.getField(label))).then((fields) =>
      fields.every((field) => !field.disabled),
    );
  }
}
