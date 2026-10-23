import { Button } from '@vaadin/react-components';
import { useSignal, type ValueSignal } from '@vaadin/hilla-react-signals';
import type Item from 'Frontend/generated/com/vaadin/hilla/test/signals/service/ListSignalService/Item.js';
import { ListSignalService } from 'Frontend/generated/endpoints.js';

const items = ListSignalService.items()!;

// Each entry of the list is a signal of its own, so an entry can be rendered
// and updated without going through the list it belongs to.
function ItemRow({ item }: { readonly item: ValueSignal<Item> }) {
  const value = item.value;

  if (!value) {
    return null;
  }

  return (
    <div data-testid="item" style={{ marginBottom: '4px' }}>
      <span id={`text-${item.id}`}>{value.text}</span>
      {' - '}
      <span id={`status-${item.id}`}>{value.completed ? 'done' : 'pending'}</span>{' '}
      <Button
        id={`toggle-${item.id}`}
        data-testid="toggleBtn"
        onClick={() => {
          item.value = { ...value, completed: !value.completed };
        }}
      >
        Toggle
      </Button>
      <Button id={`remove-${item.id}`} data-testid="removeBtn" onClick={() => items.remove(item)}>
        Remove
      </Button>
    </div>
  );
}

export default function SharedListSignalView() {
  const newItemText = useSignal('');

  return (
    <div>
      <h3 id="itemCount">Count: {items.value.length}</h3>

      <div id="itemList">
        {items.value.map((item) => (
          <ItemRow key={item.id} item={item} />
        ))}
      </div>

      <br />
      <input
        id="newItemInput"
        value={newItemText.value}
        onChange={(e) => {
          newItemText.value = e.target.value;
        }}
        placeholder="New item text"
      />
      <Button
        id="addItemBtn"
        onClick={() => {
          const text = newItemText.value.trim();
          if (text) {
            items.insertLast({ text, completed: false });
            newItemText.value = '';
          }
        }}
      >
        Add Item
      </Button>
      <Button id="clearBtn" onClick={() => items.clear()}>
        Clear All
      </Button>
    </div>
  );
}
