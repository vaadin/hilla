import { Button } from '@hilla/react-components/Button.js';
import { Icon } from '@hilla/react-components/Icon.js';
import { useContext, type JSX } from 'react';
import { AutoCrudContext } from './autocrud-context';

type GridBodyReactRendererProps<TItem> = {
  item: TItem;
};

export default function AutoCrudActions({ item }: GridBodyReactRendererProps<any>): JSX.Element {
  const context = useContext(AutoCrudContext)!;
  return (
    <span className="auto-crud-actions">
      <Button theme="tertiary small icon" aria-label="Edit" onClick={() => context.editItem(item)}>
        <Icon icon="lumo:edit" />
      </Button>
      {!context.noDelete && (
        <Button theme="tertiary small error icon" aria-label="Delete" onClick={() => context.deleteItem(item)}>
          <Icon icon="lumo:cross" />
        </Button>
      )}
    </span>
  );
}
