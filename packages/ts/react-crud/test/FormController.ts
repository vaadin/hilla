import type { FormLayoutElement } from '@hilla/react-components/FormLayout';
import { screen, waitFor } from '@testing-library/react';
import type userEvent from '@testing-library/user-event';

export type FormElement = HTMLElement & {
  value: unknown;
  disabled: boolean;
  checked?: boolean;
};

export default class FormController {
  readonly instance: FormLayoutElement;
  readonly #user: ReturnType<(typeof userEvent)['setup']>;
  readonly renderResult: HTMLElement;

  static async init(user: ReturnType<(typeof userEvent)['setup']>, source = document.body): Promise<FormController> {
    const form = await waitFor(() => source.querySelector('vaadin-form-layout')!);
    return new FormController(form, source, user);
  }

  private constructor(
    instance: FormLayoutElement,
    renderResult: HTMLElement,
    user: ReturnType<(typeof userEvent)['setup']>,
  ) {
    this.instance = instance;
    this.renderResult = renderResult;
    this.#user = user;
  }

  async getField(label: string): Promise<FormElement> {
    return (await screen.findByLabelText(label)).parentElement as FormElement;
  }

  async getFields(...labels: readonly string[]): Promise<readonly FormElement[]> {
    return await Promise.all(labels.map(async (label) => await this.getField(label)));
  }

  async findButton(text: string): Promise<HTMLButtonElement> {
    return await screen.findByText(text);
  }

  async typeInField(label: string, value: string): Promise<void> {
    const field = await screen.findByLabelText(label);
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

  async getValues(...labels: readonly string[]): Promise<readonly unknown[]> {
    return await Promise.all(labels.map(async (label) => await this.getField(label))).then((fields) =>
      fields.map((field) => (field.tagName === 'VAADIN-CHECKBOX' ? `${field.checked}` : field.value)),
    );
  }

  async areEnabled(...labels: readonly string[]): Promise<boolean> {
    return await Promise.all(labels.map(async (label) => await this.getField(label))).then((fields) =>
      fields.every((field) => !field.disabled),
    );
  }
}
