import type { AbstractModel, DetachedModelConstructor } from '@hilla/form';
import { Button } from '@hilla/react-components/Button.js';
import type { GridElement } from '@hilla/react-components/Grid.js';
import { GridColumn } from '@hilla/react-components/GridColumn.js';
import { HorizontalLayout } from '@hilla/react-components/HorizontalLayout.js';
import { VerticalLayout } from '@hilla/react-components/VerticalLayout.js';
import { type JSX, useState } from 'react';
import { AutoCrudContext } from './autocrud-context.js';
import DeleteButton from './autocrud-delete.js';
import { defaultItem, ExperimentalAutoForm } from './autoform.js';
import { AutoGrid } from './autogrid.js';
import type { CrudService } from './crud.js';
import { getProperties } from './property-info.js';

export type AutoCrudProps<TItem> = Readonly<{
  service: CrudService<TItem>;
  model: DetachedModelConstructor<AbstractModel<TItem>>;
  noDelete?: boolean;
  header?: string;
}>;

export function ExperimentalAutoCrud<TItem>({ service, model, noDelete, header }: AutoCrudProps<TItem>): JSX.Element {
  const [item, setItem] = useState<TItem | typeof defaultItem | undefined>(undefined);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const customColumns = [];
  if (!noDelete) {
    customColumns.push(<GridColumn key="deletebutton" autoWidth renderer={DeleteButton}></GridColumn>);
  }

  return (
    <>
      <AutoCrudContext.Provider
        value={{ service, properties: getProperties(model), refreshGrid: () => setRefreshTrigger(refreshTrigger + 1) }}
      >
        <VerticalLayout style={{ flex: 1 }}>
          <HorizontalLayout
            style={{
              width: '100%',
              justifyContent: header ? 'space-between' : 'end',
              paddingLeft: 'var(--lumo-space-m)',
              paddingRight: 'var(--lumo-space-m)',
              paddingBottom: 'var(--lumo-space-s)',
              paddingTop: 'var(--lumo-space-s)',
              alignItems: 'center',
            }}
          >
            {header ? <h2 style={{ fontSize: 'var(--lumo-font-size-l)' }}>{header}</h2> : <></>}
            <Button theme="primary" onClick={() => setItem(defaultItem)}>
              + New
            </Button>
          </HorizontalLayout>
          <HorizontalLayout style={{ width: '100%' }}>
            <AutoGrid
              refreshTrigger={refreshTrigger}
              service={service}
              model={model}
              onActiveItemChanged={(e) => {
                const activeItem = e.detail.value;
                (e.target as GridElement).selectedItems = activeItem ? [activeItem] : [];
              }}
              onSelectedItemsChanged={(e) => {
                if (e.detail.value.length === 0) {
                  setItem(undefined);
                } else {
                  const selectedItem = e.detail.value[0];
                  setItem({ ...selectedItem });
                }
              }}
              customColumns={customColumns}
            ></AutoGrid>
            <ExperimentalAutoForm
              disabled={!item}
              service={service}
              model={model}
              item={item}
              afterSubmit={({ item: submittedItem }) => {
                setItem(submittedItem);
                // Trigger grid data refresh
                setRefreshTrigger(refreshTrigger + 1);
              }}
            />
          </HorizontalLayout>
        </VerticalLayout>
      </AutoCrudContext.Provider>
    </>
  );
}
