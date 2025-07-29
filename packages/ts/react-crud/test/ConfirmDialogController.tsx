import { waitFor } from '@testing-library/react';
import type userEvent from '@testing-library/user-event';
import type { ConfirmDialogElement } from '@vaadin/react-components/ConfirmDialog.js';
import { expect } from 'vitest';

export default class ConfirmDialogController {
  readonly instance: ConfirmDialogElement;
  readonly #user: ReturnType<(typeof userEvent)['setup']>;

  static async init(
    container: HTMLElement,
    user: ReturnType<(typeof userEvent)['setup']>,
  ): Promise<ConfirmDialogController> {
    const dialog = await waitFor(() => {
      const el = container.querySelector('vaadin-confirm-dialog')!;
      expect(el).to.be.instanceOf(Element);
      return el;
    });
    await waitFor(() => {
      // eslint-disable-next-line @typescript-eslint/no-unused-expressions
      expect(dialog.hasAttribute('opening')).to.be.false;
    });
    return new ConfirmDialogController(dialog, user);
  }

  private constructor(instance: ConfirmDialogElement, user: ReturnType<(typeof userEvent)['setup']>) {
    this.instance = instance;
    this.#user = user;
    // @ts-expect-error: Vaadin overlay API
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    this.instance.$.overlay.modeless = true;
  }

  get text(): string {
    return this.instance.textContent ?? '';
  }

  async confirm(): Promise<void> {
    const btn = this.instance.querySelector("[slot='confirm-button']")!;
    await this.#user.click(btn);
    await waitFor(() => {
      // eslint-disable-next-line @typescript-eslint/no-unused-expressions
      expect(this.instance.hasAttribute('opened')).to.be.false;
    });
  }

  async cancel(): Promise<void> {
    const btn = this.instance.querySelector("[slot='cancel-button']")!;
    await this.#user.click(btn);
    await waitFor(() => {
      // eslint-disable-next-line @typescript-eslint/no-unused-expressions
      expect(this.instance.hasAttribute('opened')).to.be.false;
    });
  }
}
