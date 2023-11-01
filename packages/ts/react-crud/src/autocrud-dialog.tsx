import { Button } from '@hilla/react-components/Button';
import { Dialog } from '@hilla/react-components/Dialog';
import { Icon } from '@hilla/react-components/Icon';
import type { JSX } from 'react';

// eslint-disable-next-line
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';

interface AutoCrudDialogProps {
  children: React.ReactElement;
  opened: boolean;
  // eslint-disable-next-line @typescript-eslint/method-signature-style
  onClose: () => void;
}

export function AutoCrudDialog(props: AutoCrudDialogProps): JSX.Element {
  const { children, opened, onClose } = props;
  return (
    <Dialog
      overlayClass="auto-crud-dialog"
      opened={opened}
      headerTitle="Edit item"
      headerRenderer={() => (
        <Button theme="tertiary" onClick={onClose} aria-label="Close">
          <Icon icon="lumo:cross" style={{ height: 'var(--lumo-icon-size-l)', width: 'var(--lumo-icon-size-l)' }} />
        </Button>
      )}
    >
      {children}
    </Dialog>
  );
}
