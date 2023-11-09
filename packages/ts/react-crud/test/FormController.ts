import type { FormLayoutElement } from '@hilla/react-components/FormLayout';
import { waitFor, within } from '@testing-library/react';
import type userEvent from '@testing-library/user-event';

export type FormElement = HTMLElement & {
  value: unknown;
  disabled: boolean;
  checked?: boolean;
};

export default class FormController {
  readonly instance: HTMLElement;
  readonly formLayout: FormLayoutElement;
  readonly #user: ReturnType<(typeof userEvent)['setup']>;
  readonly renderResult: HTMLElement;

  static async init(user: ReturnType<(typeof userEvent)['setup']>, source = document.body): Promise<FormController> {
    const form = await waitFor(() => within(source).getByTestId('auto-form'));
    return new FormController(form, source, user);
  }

  private constructor(instance: HTMLElement, renderResult: HTMLElement, user: ReturnType<(typeof userEvent)['setup']>) {
    this.instance = instance;
    this.formLayout = instance.querySelector('vaadin-form-layout')!;
    this.renderResult = renderResult;
    this.#user = user;
  }

  async getField(label: string): Promise<FormElement> {
    return (await within(this.instance).findByLabelText(label)).parentElement as FormElement;
  }

  queryField(label: string): FormElement | undefined {
    return within(this.instance).queryByLabelText(label)?.parentElement as FormElement | undefined;
  }

  async getFields(...labels: readonly string[]): Promise<readonly FormElement[]> {
    return await Promise.all(labels.map(async (label) => await this.getField(label)));
  }

  async findButton(text: string): Promise<HTMLButtonElement> {
    return await within(this.instance).findByRole('button', { name: text });
  }

  queryButton(text: string): HTMLButtonElement | null {
    return within(this.instance).queryByRole('button', { name: text });
  }

  async typeInField(label: string, value: string): Promise<void> {
    const field = await within(this.instance).findByLabelText(label);
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
