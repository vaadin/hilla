import type { AbstractModel, DetachedModelConstructor } from '@hilla/form';
import type { GridElement } from '@hilla/react-components/Grid.js';
import { HorizontalLayout } from '@hilla/react-components/HorizontalLayout.js';
import { createContext, useState, type JSX } from 'react';
import { ExperimentalAutoForm } from './autoform';
import { AutoGrid } from './autogrid';
import type { CrudService } from './crud';

export type AutoCrudProps<TItem> = Readonly<{
  service: CrudService<TItem>;
  model: DetachedModelConstructor<AbstractModel<TItem>>;
}>;

interface AutoCrudContextType<TItem> {
  item: TItem | undefined;
}

export const AutoCrudContext = createContext<AutoCrudContextType<any>>({ item: undefined });

export function ExperimentalAutoCrud<TItem>({ service, model }: AutoCrudProps<TItem>): JSX.Element {
  const [item, setItem] = useState<TItem | undefined>(undefined);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  return (
    <>
      <HorizontalLayout>
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
        ></ExperimentalAutoForm>
      </HorizontalLayout>
    </>
  );
}
