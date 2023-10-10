import { Button } from '@hilla/react-components/Button.js';

import { ConfirmDialog } from '@hilla/react-components/ConfirmDialog.js';
import { useContext, useState, type JSX } from 'react';
import { AutoCrudContext } from './autocrud-context';
import { getIdProperty } from './property-info';

type GridBodyReactRendererProps<TItem> = {
  item: TItem;
};

export default function DeleteButton({ item }: GridBodyReactRendererProps<any>): JSX.Element {
  const context = useContext(AutoCrudContext)!;
  const [opened, setOpened] = useState(false);

  async function deleteItem(): Promise<void> {
    const idProperty = getIdProperty(context.properties)!;
    // eslint-disable-next-line
    const id = item[idProperty.name];
    await context.service.delete(id);
    context.refreshGrid();
  }

  return (
    <>
      <Button theme="tertiary" onClick={() => setOpened(true)}>
        Delete
      </Button>
      <ConfirmDialog
        opened={opened}
        header="Delete item"
        confirmTheme="error"
        // eslint-disable-next-line
        onConfirm={async () => {
          await deleteItem();
          setOpened(false);
        }}
        cancelButtonVisible
        onCancel={() => setOpened(false)}
      >
        Are you sure you want to delete the selected item?
      </ConfirmDialog>
    </>
  );
}
