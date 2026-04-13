import { Button } from '@vaadin/react-components';
import { useSignal } from '@vaadin/hilla-react-signals';
import { ListSignalService } from 'Frontend/generated/endpoints.js';

const items = ListSignalService.items()!;

export default function SharedListSignalView() {
  const newItemText = useSignal('');

  return (
    <div>
      <h3 id="itemCount">Count: {items.value.length}</h3>

      <div id="itemList">
        {items.value.map((itemSignal) => {
          const item = itemSignal.value;
          if (!item) return null;
          return (
            <div key={itemSignal.id} data-testid="item" style={{ marginBottom: '4px' }}>
              <span id={`text-${itemSignal.id}`}>{item.text}</span>
              {' - '}
              <span id={`status-${itemSignal.id}`}>{item.completed ? 'done' : 'pending'}</span>
              {' '}
              <Button
                id={`toggle-${itemSignal.id}`}
                data-testid="toggleBtn"
                onClick={() => {
                  itemSignal.value = { ...item, completed: !item.completed };
                }}
              >
                Toggle
              </Button>
              <Button
                id={`remove-${itemSignal.id}`}
                data-testid="removeBtn"
                onClick={() => items.remove(itemSignal)}
              >
                Remove
              </Button>
            </div>
          );
        })}
      </div>

      <br />
      <input
        id="newItemInput"
        value={newItemText.value}
        onChange={(e) => { newItemText.value = e.target.value; }}
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
