import type { AbstractModel, DetachedModelConstructor } from '@hilla/form';
import { Button } from '@hilla/react-components/Button.js';
import { type JSX, useState } from 'react';
import { AutoCrudDialog } from './autocrud-dialog';
import css from './autocrud.obj.css';
import { type AutoFormProps, emptyItem, ExperimentalAutoForm } from './autoform.js';
import { AutoGrid, type AutoGridProps } from './autogrid.js';
import type { CrudService } from './crud.js';
import { useMediaQuery } from './media-query';
import type { ComponentStyleProps } from './util';

document.adoptedStyleSheets.unshift(css);

export type AutoCrudFormProps<TItem> = Omit<
  Partial<AutoFormProps<AbstractModel<TItem>>>,
  'afterSubmit' | 'disabled' | 'item' | 'model' | 'service'
>;

export type AutoCrudGridProps<TItem> = Omit<
  Partial<AutoGridProps<TItem>>,
  'model' | 'onActiveItemChanged' | 'refreshTrigger' | 'selectedItems' | 'service'
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
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const fullScreen = useMediaQuery('(max-width: 600px), (max-height: 600px)');

  function refreshGrid() {
    setRefreshTrigger(refreshTrigger + 1);
  }

  function editItem(itemToEdit: TItem) {
    setItem(itemToEdit);
  }

  function handleCancel() {
    setItem(undefined);
  }

  const autoForm = (
    <ExperimentalAutoForm
      {...formProps}
      disabled={!item}
      service={service}
      model={model}
      item={item}
      deleteButtonVisible={!noDelete}
      afterSubmit={({ item: submittedItem }) => {
        if (fullScreen) {
          setItem(undefined);
        } else {
          setItem(submittedItem);
        }
        refreshGrid();
      }}
      afterDelete={() => {
        setItem(undefined);
        refreshGrid();
      }}
    />
  );

  return (
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
  );
}
