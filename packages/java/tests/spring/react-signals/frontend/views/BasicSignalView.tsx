import { signal } from '@vaadin/hilla-react-signals';
import { TextField } from '@vaadin/react-components';

const name = signal('');

export default function BasicSignalView() {
  return (
    <>
      <section className="flex p-m gap-m items-end">
        <TextField
          label="Name"
          onValueChanged={(e) => {
            name.value = e.detail.value;
          }}
        />
        <span id="out">
          <>Echo: {name}</>
        </span>
      </section>
    </>
  );
}
