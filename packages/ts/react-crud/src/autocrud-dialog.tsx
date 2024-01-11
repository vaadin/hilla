import { Button } from '@vaadin/react-components/Button';
import { Dialog } from '@vaadin/react-components/Dialog';
import { Icon } from '@vaadin/react-components/Icon';
import type { JSX } from 'react';

// eslint-disable-next-line
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';

interface AutoCrudDialogProps {
  header: JSX.Element | null | undefined;
  children: React.ReactElement;
  opened: boolean;
  // eslint-disable-next-line @typescript-eslint/method-signature-style
  onClose: () => void;
}

export function AutoCrudDialog(props: AutoCrudDialogProps): JSX.Element {
  const { header, children, opened, onClose } = props;
  return (
    <Dialog
      overlayClass="auto-crud-dialog"
      opened={opened}
      headerRenderer={() => (
        <div className="auto-crud-dialog-header">
          {header}
          <Button theme="tertiary" onClick={onClose} aria-label="Close">
            <Icon icon="lumo:cross" style={{ height: 'var(--lumo-icon-size-l)', width: 'var(--lumo-icon-size-l)' }} />
          </Button>
        </div>
      )}
    >
      {children}
    </Dialog>
  );
}
