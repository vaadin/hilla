import { ArrayModel, BooleanModel, NumberModel, StringModel } from '@vaadin/hilla-lit-form';
import type { SignalMethodOptions } from '@vaadin/hilla-react-signals';
import { ListSignal, NumberSignal, ValueSignal } from '@vaadin/hilla-react-signals';
import type Sample from './com/vaadin/hilla/parser/plugins/sample/SampleEndpoint/Sample.js';
import SampleModel from './com/vaadin/hilla/parser/plugins/sample/SampleEndpoint/SampleModel.js';
import client from './connect-client.default.js';

export function channel(name: string | undefined): ListSignal<Sample | undefined> | undefined {
  return new ListSignal({
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'channel',
    params: { name },
  });
}

export function chosen(
  options: string | undefined,
  _options?: SignalMethodOptions<string | undefined>,
): ValueSignal<string | undefined> | undefined {
  return new ValueSignal(_options?.defaultValue, {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'chosen',
    params: { options },
  });
}

export function counter(): NumberSignal | undefined {
  return new NumberSignal(0, {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'counter',
  });
}

export function counterOf(name: string | undefined): NumberSignal | undefined {
  return new NumberSignal(0, {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'counterOf',
    params: { name },
  });
}

export function crowd(
  options?: SignalMethodOptions<Array<Sample | undefined>>,
): ValueSignal<Array<Sample | undefined>> {
  return new ValueSignal(options?.defaultValue ?? ArrayModel.createEmptyValue(), {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'crowd',
  });
}

export function current(options?: SignalMethodOptions<Sample>): ValueSignal<Sample> {
  return new ValueSignal(options?.defaultValue ?? SampleModel.createEmptyValue(), {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'current',
  });
}

export function enabled(options?: SignalMethodOptions<boolean>): ValueSignal<boolean> {
  return new ValueSignal(options?.defaultValue ?? BooleanModel.createEmptyValue(), {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'enabled',
  });
}

export function limit(options?: SignalMethodOptions<number>): ValueSignal<number> {
  return new ValueSignal(options?.defaultValue ?? NumberModel.createEmptyValue(), {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'limit',
  });
}

export function name(options?: SignalMethodOptions<string | undefined>): ValueSignal<string | undefined> | undefined {
  return new ValueSignal(options?.defaultValue, {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'name',
  });
}

export function names(): ListSignal<string | undefined> | undefined {
  return new ListSignal({
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'names',
  });
}

export function pending(options?: SignalMethodOptions<string | undefined>): ValueSignal<string | undefined> {
  return new ValueSignal(options?.defaultValue, {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'pending',
  });
}

export function sample(
  detailed: boolean,
  options?: SignalMethodOptions<Sample | undefined>,
): ValueSignal<Sample | undefined> | undefined {
  return new ValueSignal(options?.defaultValue, {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'sample',
    params: { detailed },
  });
}

export function title(options?: SignalMethodOptions<string>): ValueSignal<string> {
  return new ValueSignal(options?.defaultValue ?? StringModel.createEmptyValue(), {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'title',
  });
}
