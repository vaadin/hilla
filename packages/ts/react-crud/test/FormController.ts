import type { RenderResult } from '@testing-library/react';
import { waitFor } from '@testing-library/react';
import type userEvent from '@testing-library/user-event';
import { FormFieldController } from './FormFieldController.js';

export type FormElement = HTMLElement & {
  value: unknown;
  disabled: boolean;
  checked?: boolean;
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

  async getFieldByLabel(label: string): Promise<FormElement> {
    return (await this.#result.findByLabelText(label)) as FormElement;
  }

  async getFieldsByLabels(...labels: readonly string[]): Promise<readonly FormElement[]> {
    return await Promise.all(labels.map(async (label) => await this.getFieldByLabel(label)));
  }

  async getFieldByTestId(testId: string): Promise<FormElement> {
    return (await this.#result.findByTestId(testId)) as FormElement;
  }

  async getFieldsByTestIds(...testIds: readonly string[]): Promise<readonly FormElement[]> {
    return await Promise.all(testIds.map(async (testId) => await this.getFieldByTestId(testId)));
  }

  async findButton(text: string): Promise<HTMLButtonElement> {
    return (await this.#result.findByText(text)) as HTMLButtonElement;
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

  async getValues(...testIds: readonly string[]): Promise<readonly unknown[]> {
    return await Promise.all(testIds.map(async (testId) => await this.getFieldByTestId(testId))).then((fields) =>
      fields.map((field) => (field.tagName === 'VAADIN-CHECKBOX' ? `${field.checked}` : field.value)),
    );
  }

  async areEnabled(...testIds: readonly string[]): Promise<boolean> {
    return await Promise.all(testIds.map(async (testId) => await this.getFieldByTestId(testId))).then((fields) =>
      fields.every((field) => !field.disabled),
    );
  }
}
