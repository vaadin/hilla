import { $defaultValue, ArrayModel, BooleanModel, NumberModel, StringModel } from '@vaadin/hilla-models';
import type { SignalMethodOptions } from '@vaadin/hilla-react-signals';
import { ListSignal, NumberSignal, ValueSignal } from '@vaadin/hilla-react-signals';
import type Sample from './com/vaadin/hilla/parser/plugins/sample/SampleEndpoint/Sample.js';
import SampleModel from './com/vaadin/hilla/parser/plugins/sample/SampleEndpoint/SampleModel.js';
import client from './connect-client.default.js';

function channel(name: string | undefined): ListSignal<Sample | undefined> | undefined {
  return new ListSignal({
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'channel',
    params: { name },
  });
}

function chosen(options: string | undefined, _options?: SignalMethodOptions<string | undefined>): ValueSignal<string | undefined> | undefined {
  return new ValueSignal(_options?.defaultValue, {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'chosen',
    params: { options },
  });
}

function counter(): NumberSignal | undefined {
  return new NumberSignal(0, {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'counter',
  });
}

function counterOf(name: string | undefined): NumberSignal | undefined {
  return new NumberSignal(0, {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'counterOf',
    params: { name },
  });
}

function crowd(options?: SignalMethodOptions<Array<Sample | undefined>>): ValueSignal<Array<Sample | undefined>> {
  return new ValueSignal(options?.defaultValue ?? ArrayModel[$defaultValue], {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'crowd',
  });
}

function current(options?: SignalMethodOptions<Sample>): ValueSignal<Sample> {
  return new ValueSignal(options?.defaultValue ?? SampleModel[$defaultValue], {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'current',
  });
}

function enabled(options?: SignalMethodOptions<boolean>): ValueSignal<boolean> {
  return new ValueSignal(options?.defaultValue ?? BooleanModel[$defaultValue], {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'enabled',
  });
}

function limit(options?: SignalMethodOptions<number>): ValueSignal<number> {
  return new ValueSignal(options?.defaultValue ?? NumberModel[$defaultValue], {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'limit',
  });
}

function name(options?: SignalMethodOptions<string | undefined>): ValueSignal<string | undefined> | undefined {
  return new ValueSignal(options?.defaultValue, {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'name',
  });
}

function names(): ListSignal<string | undefined> | undefined {
  return new ListSignal({
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'names',
  });
}

function pending(options?: SignalMethodOptions<string | undefined>): ValueSignal<string | undefined> {
  return new ValueSignal(options?.defaultValue, {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'pending',
  });
}

function sample(detailed: boolean, options?: SignalMethodOptions<Sample | undefined>): ValueSignal<Sample | undefined> | undefined {
  return new ValueSignal(options?.defaultValue, {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'sample',
    params: { detailed },
  });
}

function title(options?: SignalMethodOptions<string>): ValueSignal<string> {
  return new ValueSignal(options?.defaultValue ?? StringModel[$defaultValue], {
    client: client,
    endpoint: 'SignalsEndpoint',
    method: 'title',
  });
}

export { channel, chosen, counter, counterOf, crowd, current, enabled, limit, name, names, pending, sample, title };
