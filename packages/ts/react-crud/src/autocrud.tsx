import type { AbstractModel, DetachedModelConstructor } from '@hilla/form';
import { Button } from '@hilla/react-components/Button.js';
import { ConfirmDialog } from '@hilla/react-components/ConfirmDialog';
import { GridColumn } from '@hilla/react-components/GridColumn.js';
import { type JSX, useState } from 'react';
import AutoCrudActions from './autocrud-actions';
import { AutoCrudContext } from './autocrud-context.js';
import { AutoCrudDialog } from './autocrud-dialog';
import css from './autocrud.obj.css';
import { type AutoFormProps, emptyItem, ExperimentalAutoForm } from './autoform.js';
import { AutoGrid, type AutoGridProps } from './autogrid.js';
import type { CrudService } from './crud.js';
import { useMediaQuery } from './media-query';
import { getIdProperty, getProperties } from './property-info.js';
import type { ComponentStyleProps } from './util';

document.adoptedStyleSheets.unshift(css);

export type AutoCrudFormProps<TItem> = Omit<
  Partial<AutoFormProps<AbstractModel<TItem>>>,
  'afterSubmit' | 'disabled' | 'item' | 'model' | 'service'
>;

export type AutoCrudGridProps<TItem> = Omit<
  Partial<AutoGridProps<TItem>>,
  'customColumns' | 'model' | 'onActiveItemChanged' | 'refreshTrigger' | 'selectedItems' | 'service'
>;

export type AutoCrudProps<TItem> = ComponentStyleProps &
  Readonly<{
    service: CrudService<TItem>;
    model: DetachedModelConstructor<AbstractModel<TItem>>;
    noDelete?: boolean;
    formProps?: AutoCrudFormProps<TItem>;
    gridProps?: AutoCrudGridProps<TItem>;
  }>;

export function ExperimentalAutoCrud<TItem>({
  service,
  model,
  noDelete,
  formProps,
  gridProps,
  style,
  id,
  className,
}: AutoCrudProps<TItem>): JSX.Element {
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
      {...formProps}
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
        <div className={`auto-crud ${className}`} id={id} style={style}>
          <div className="auto-crud-main">
            <AutoGrid
              {...gridProps}
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
