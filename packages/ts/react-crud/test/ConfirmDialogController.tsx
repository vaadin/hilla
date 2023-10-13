import type { ConfirmDialogElement } from '@hilla/react-components/ConfirmDialog.js';
import { waitFor } from '@testing-library/react';
import type userEvent from '@testing-library/user-event';

export default class ConfirmDialogController {
  readonly instance: ConfirmDialogElement;
  readonly #user: ReturnType<(typeof userEvent)['setup']>;

  static async init(
    cell: HTMLElement,
    user: ReturnType<(typeof userEvent)['setup']>,
  ): Promise<ConfirmDialogController> {
    const dialog = (await waitFor(() => cell.querySelector('vaadin-confirm-dialog')))!;
    return new ConfirmDialogController(dialog, user);
  }

  private constructor(instance: ConfirmDialogElement, user: ReturnType<(typeof userEvent)['setup']>) {
    this.instance = instance;
    this.#user = user;
  }

  get text(): string {
    // @ts-expect-error: internal property
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    const content = this.instance.$.dialog.$.overlay.$.content as HTMLElement;
    return content
      .querySelector('slot')!
      .assignedNodes()
      .map((e) => (e as HTMLElement).innerText)
      .join('');
  }

  get overlay(): HTMLElement {
    // @ts-expect-error: internal property
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    return this.instance.$.dialog.$.overlay;
  }

  async confirm(): Promise<void> {
    const btn = this.overlay.querySelector("[slot='confirm-button']")!;
    await this.#user.click(btn);
  }

  async cancel(): Promise<void> {
    const btn = this.overlay.querySelector("[slot='cancel-button']")!;
    await this.#user.click(btn);
  }
}
