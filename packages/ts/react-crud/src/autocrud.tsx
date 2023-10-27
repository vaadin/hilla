import type { AbstractModel, DetachedModelConstructor } from '@hilla/form';
import { Button } from '@hilla/react-components/Button.js';
import { GridColumn } from '@hilla/react-components/GridColumn.js';
import { type JSX, useState } from 'react';
import { AutoCrudContext } from './autocrud-context.js';
import DeleteButton from './autocrud-delete.js';
import { AutoCrudDialog } from './autocrud-dialog';
import { emptyItem, ExperimentalAutoForm } from './autoform.js';
import { AutoGrid } from './autogrid.js';
import type { CrudService } from './crud.js';
import { useMediaQuery } from './media-query';
import { getProperties } from './property-info.js';

export type AutoCrudProps<TItem> = Readonly<{
  service: CrudService<TItem>;
  model: DetachedModelConstructor<AbstractModel<TItem>>;
  noDelete?: boolean;
}>;

export function ExperimentalAutoCrud<TItem>({ service, model, noDelete }: AutoCrudProps<TItem>): JSX.Element {
  const [item, setItem] = useState<TItem | typeof emptyItem | undefined>(undefined);
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const fullScreen = useMediaQuery('(max-width: 600px), (max-height: 600px)');

  const customColumns = [];
  if (!noDelete) {
    customColumns.push(<GridColumn key="deletebutton" autoWidth renderer={DeleteButton}></GridColumn>);
  }

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
        // Trigger grid data refresh
        setRefreshTrigger(refreshTrigger + 1);
      }}
    />
  );

  function handleCancel() {
    setItem(undefined);
  }

  return (
    <>
      <AutoCrudContext.Provider
        value={{ service, properties: getProperties(model), refreshGrid: () => setRefreshTrigger(refreshTrigger + 1) }}
      >
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
    </>
  );
}
