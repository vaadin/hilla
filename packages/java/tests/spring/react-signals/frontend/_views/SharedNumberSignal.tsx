import { NumberSignalServiceWrapper } from '../helper/NumberSignalServiceWrapper.js';

const counter = NumberSignalServiceWrapper.counter();
const sharedValue = NumberSignalServiceWrapper.sharedValue();

export default function SharedNumberSignal() {
  return (
    <div>
      <span id="sharedValue">Shared value: {sharedValue}</span>
      <button id="increaseSharedValue" onClick={() => (sharedValue.value += 2)}>
        Increase by 2
      </button>
      <br />
      <span id="counter">Counter value: {counter}</span>
      <button id="incrementCounter" onClick={() => counter.value++}>
        Increment
      </button>
      <br />
      <button
        id="reset"
        onClick={() => {
          sharedValue.value = 0.5;
          counter.value = 0;
        }}
      >
        Reset
      </button>
    </div>
  );
}
