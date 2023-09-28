import type { AbstractModel, DetachedModelConstructor } from '@hilla/form';
import type { GridElement } from '@hilla/react-components/Grid.js';
import { HorizontalLayout } from '@hilla/react-components/HorizontalLayout.js';
import { createContext, useState, type JSX, useRef } from 'react';
import { ExperimentalAutoForm } from './autoform';
import { AutoGrid, type AutoGridHandle } from './autogrid';
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
  const gridRef = useRef<AutoGridHandle>(null);

  return (
    <>
      <HorizontalLayout>
        <AutoGrid
          service={service}
          model={model}
          onActiveItemChanged={(e) => {
            const activeItem = e.detail.value;
            (e.target as GridElement).selectedItems = activeItem ? [activeItem] : [];
          }}
          onSelectedItemsChanged={(e) => {
            const selectedItem = e.detail.value[0];
            setItem({ ...selectedItem });
          }}
          ref={gridRef}
        ></AutoGrid>
        <ExperimentalAutoForm
          disabled={!item}
          service={service}
          model={model}
          item={item}
          onSubmit={() => {
            // Trigger grid data refresh
            gridRef.current?.refresh();
          }}
        ></ExperimentalAutoForm>
      </HorizontalLayout>
    </>
  );
}
