import { effect, NumberSignal, signal } from '@vaadin/hilla-react-signals';
import { NumberSignalService } from 'Frontend/generated/endpoints.js';
import { Button } from '@vaadin/react-components/Button.js';

const isHigh = signal(true);

let sharedValue: NumberSignal;
effect(() => {
  sharedValue = NumberSignalService.numberSignal(isHigh.value);
});

export default function ServiceMethodParams() {
  return (
    <div>
      <h3 id="valueH3">
        {isHigh.value ? 'High' : 'Low'} Value: <span id="valueSpan">{sharedValue.value}</span>
      </h3>
      <Button id="increaseDecreaseBtn" onClick={() => sharedValue.increment(isHigh.value ? 1 : -1)}>
        {isHigh.value ? 'Increase' : 'Decrease'}
      </Button>
      <br />
      <Button id="toggleBtn" onClick={() => (isHigh.value = !isHigh.value)}>
        Toggle High/Low
      </Button>
    </div>
  );
}
