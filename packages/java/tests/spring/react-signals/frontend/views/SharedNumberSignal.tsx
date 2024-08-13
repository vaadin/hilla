import { Button } from '@vaadin/react-components';
import { useSignal } from '@vaadin/hilla-react-signals';
import { NumberSignalService } from 'Frontend/generated/endpoints.js';

const counter = NumberSignalService.counter();
const sharedValue = NumberSignalService.sharedValue();

export default function SharedNumberSignal() {
  const sharedValueFromServer = useSignal<number | undefined>(0.0);
  const counterValueFromServer = useSignal<number | undefined>(0);

  return (
    <div>
      <span id="sharedValue">{sharedValue}</span>
      <Button id="increaseSharedValue" onClick={() => (sharedValue.value += 2)}>
        Increase by 2
      </Button>
      <br />
      <span id="counter">{counter}</span>
      <Button id="incrementCounter" onClick={() => counter.value++}>
        Increment
      </Button>
      <br />
      <Button
        id="reset"
        onClick={() => {
          sharedValue.value = 0.5;
          counter.value = 0;
        }}
      >
        Reset
      </Button>
      <br />
      <span id="sharedValueFromServer">{sharedValueFromServer}</span>
      <Button
        id="fetchSharedValue"
        onClick={async () => {
          sharedValueFromServer.value = await NumberSignalService.fetchSharedValue();
        }}
      >
        Fetch shared value from server
      </Button>
      <br />
      <span id="counterValueFromServer">{counterValueFromServer}</span>
      <Button
        id="fetchCounterValue"
        onClick={async () => {
          counterValueFromServer.value = await NumberSignalService.fetchCounterValue();
        }}
      >
        Fetch counter value from server
      </Button>
    </div>
  );
}
