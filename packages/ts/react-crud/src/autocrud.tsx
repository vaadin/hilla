import type { AbstractModel, DetachedModelConstructor } from '@hilla/form';
import { Button } from '@hilla/react-components/Button.js';
import { ConfirmDialog } from '@hilla/react-components/ConfirmDialog';
import { GridColumn } from '@hilla/react-components/GridColumn.js';
import { type JSX, useState } from 'react';
import AutoCrudActions from './autocrud-actions';
import { AutoCrudContext } from './autocrud-context.js';
import { AutoCrudDialog } from './autocrud-dialog';
import css from './autocrud.obj.css';
import { emptyItem, ExperimentalAutoForm } from './autoform.js';
import { AutoGrid } from './autogrid.js';
import type { CrudService } from './crud.js';
import { useMediaQuery } from './media-query';
import { getIdProperty, getProperties } from './property-info.js';

document.adoptedStyleSheets.unshift(css);

export type AutoCrudProps<TItem> = Readonly<{
  service: CrudService<TItem>;
  model: DetachedModelConstructor<AbstractModel<TItem>>;
  noDelete?: boolean;
}>;

export function ExperimentalAutoCrud<TItem>({ service, model, noDelete }: AutoCrudProps<TItem>): JSX.Element {
  const [item, setItem] = useState<TItem | typeof emptyItem | undefined>(undefined);
  const [pendingItemToDelete, setPendingItemToDelete] = useState<TItem | undefined>(undefined);
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const fullScreen = useMediaQuery('(max-width: 600px), (max-height: 600px)');

  function refreshGrid() {
    setRefreshTrigger(refreshTrigger + 1);
  }

  function editItem(itemToEdit: TItem) {
    setItem(itemToEdit);
  }

  function deleteItem(itemToDelete: TItem) {
    setPendingItemToDelete(itemToDelete);
  }

  async function confirmDelete() {
    const properties = getProperties(model);
    const idProperty = getIdProperty(properties)!;
    // eslint-disable-next-line
    const id = (pendingItemToDelete as any)[idProperty.name];
    await service.delete(id);
    setPendingItemToDelete(undefined);
    if (item === pendingItemToDelete) {
      setItem(undefined);
    }
    refreshGrid();
  }

  function cancelDelete() {
    setPendingItemToDelete(undefined);
  }

  function handleCancel() {
    setItem(undefined);
  }

  const customColumns = [
    <GridColumn key="actions-column" width="90px" flexGrow={0} frozenToEnd renderer={AutoCrudActions}></GridColumn>,
  ];

  const autoForm = (
    <ExperimentalAutoForm
      disabled={!item}
      service={service}
      model={model}
      item={item}
      afterSubmit={({ item: submittedItem }) => {
        if (fullScreen) {
          setItem(undefined);
        } else {
          setItem(submittedItem);
        }
        refreshGrid();
      }}
    />
  );

  return (
    <>
      <AutoCrudContext.Provider value={{ noDelete, editItem, deleteItem }}>
        <div className="auto-crud">
          <div className="auto-crud-main">
            <AutoGrid
              refreshTrigger={refreshTrigger}
              service={service}
              model={model}
              selectedItems={item && item !== emptyItem ? [item] : []}
              onActiveItemChanged={(e) => {
                const activeItem = e.detail.value;
                setItem(activeItem ?? undefined);
              }}
              customColumns={customColumns}
            ></AutoGrid>
            <div className="auto-crud-toolbar">
              <Button theme="primary" onClick={() => setItem(emptyItem)}>
                + New
              </Button>
            </div>
          </div>

          {fullScreen ? (
            <AutoCrudDialog opened={!!item} onClose={handleCancel}>
              {autoForm}
            </AutoCrudDialog>
          ) : (
            autoForm
          )}
        </div>
      </AutoCrudContext.Provider>
      {pendingItemToDelete && (
        <ConfirmDialog
          opened
          header="Delete item"
          confirmTheme="error"
          cancelButtonVisible
          // eslint-disable-next-line
          onConfirm={confirmDelete}
          onCancel={cancelDelete}
        >
          Are you sure you want to delete the selected item?
        </ConfirmDialog>
      )}
    </>
  );
}
