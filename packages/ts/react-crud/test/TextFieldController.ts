import type { TextFieldElement } from '@hilla/react-components/TextField.js';
import { waitFor } from '@testing-library/react';
import type userEvent from '@testing-library/user-event';

export default class TextFieldController {
  readonly instance: TextFieldElement;
  readonly #user: ReturnType<(typeof userEvent)['setup']>;

  static async initByParent(
    parent: HTMLElement,
    user: ReturnType<(typeof userEvent)['setup']>,
    name = 'vaadin-text-field',
  ): Promise<TextFieldController> {
    const instance = await waitFor(() => parent.querySelector(name)!);
    return new TextFieldController(instance as TextFieldElement, user);
  }

  static async initByLabel(
    child: HTMLElement,
    user: ReturnType<(typeof userEvent)['setup']>,
  ): Promise<TextFieldController> {
    const instance = await waitFor(() => child.parentElement!);
    return new TextFieldController(instance as TextFieldElement, user);
  }

  private constructor(instance: TextFieldElement, user: ReturnType<(typeof userEvent)['setup']>) {
    this.instance = instance;
    this.#user = user;
  }

  get value(): string {
    return this.instance.value;
  }

  get disabled(): boolean {
    return this.instance.disabled;
  }

  async type(value: string): Promise<void> {
    await this.#user.dblClick(this.instance.inputElement);
    await this.#user.keyboard(value);
  }
}
