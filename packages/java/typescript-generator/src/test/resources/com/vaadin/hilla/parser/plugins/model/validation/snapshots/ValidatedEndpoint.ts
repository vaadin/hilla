import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type Validated from './com/vaadin/hilla/parser/plugins/model/validation/ValidatedEndpoint/Validated.js';
import client from './connect-client.default.js';

async function save(entity: Validated | undefined, init?: EndpointRequestInit): Promise<void> {
  return client.call('ValidatedEndpoint', 'save', { entity }, init);
}

export { save };
