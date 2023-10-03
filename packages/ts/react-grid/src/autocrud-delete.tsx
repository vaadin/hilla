import { Button } from '@hilla/react-components/Button.js';
import { ConfirmDialog } from '@hilla/react-components/ConfirmDialog.js';
import { useContext, useState, type JSX } from 'react';
import { AutoCrudContext } from './autocrud-context';
import { getIdProperty } from './property-info';

export default function DeleteButton(renderContext: any): JSX.Element {
  const context = useContext(AutoCrudContext)!;
  const [opened, setOpened] = useState(false);

  async function deleteItem(): Promise<void> {
    const idProperty = getIdProperty(context.properties!)!;
    await context.service!.delete(renderContext.item[idProperty.name]);
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
        onConfirm={() => {
          deleteItem();
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
