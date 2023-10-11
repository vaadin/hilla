import type { ButtonElement } from '@hilla/react-components/Button.js';
import type { ConfirmDialogElement } from '@hilla/react-components/ConfirmDialog.js';

type HasDollar = {
  $: Record<string, HasDollar & HTMLElement>;
};
type TestConfirmDialog = {
  _getDialogText(): string;
  _getConfirmButton(): ButtonElement;
  _getCancelButton(): ButtonElement;
};

export function findConfirmDialog(
  context: HTMLElement,
): (ConfirmDialogElement & HasDollar & TestConfirmDialog) | undefined {
  const confirmDialog = context.querySelector('vaadin-confirm-dialog') as
    | (ConfirmDialogElement & HasDollar & TestConfirmDialog)
    | undefined;
  if (!confirmDialog) {
    return undefined;
  }

  confirmDialog._getDialogText = () => {
    // eslint-disable-next-line
    const content = confirmDialog.$.dialog.$.overlay.$.content as HTMLElement;
    return content
      .querySelector('slot')!
      .assignedNodes()
      .map((e) => (e as HTMLElement).innerText)
      .join('');
  };

  // eslint-disable-next-line
  const { overlay } = confirmDialog.$.dialog.$;
  confirmDialog._getCancelButton = () => overlay.querySelector("[slot='cancel-button']")!;
  confirmDialog._getConfirmButton = () => overlay.querySelector("[slot='confirm-button']")!;
  return confirmDialog;
}
