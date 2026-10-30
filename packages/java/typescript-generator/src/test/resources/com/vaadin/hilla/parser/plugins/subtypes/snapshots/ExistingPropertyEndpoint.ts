import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
import type DeclaredBase from './com/vaadin/hilla/parser/plugins/subtypes/existingproperty/DeclaredBase.js';
import type EnumDiscriminated from './com/vaadin/hilla/parser/plugins/subtypes/existingproperty/EnumDiscriminated.js';
import type InheritedBase from './com/vaadin/hilla/parser/plugins/subtypes/existingproperty/InheritedBase.js';
import type InheritedEnum from './com/vaadin/hilla/parser/plugins/subtypes/existingproperty/InheritedEnum.js';
import client from './connect-client.default.js';

async function send(init?: EndpointRequestInit): Promise<DeclaredBase | undefined> {
  return client.call('ExistingPropertyEndpoint', 'send', {}, init);
}

async function sendEnumDiscriminated(init?: EndpointRequestInit): Promise<EnumDiscriminated | undefined> {
  return client.call('ExistingPropertyEndpoint', 'sendEnumDiscriminated', {}, init);
}

async function sendInherited(init?: EndpointRequestInit): Promise<InheritedBase | undefined> {
  return client.call('ExistingPropertyEndpoint', 'sendInherited', {}, init);
}

async function sendInheritedEnum(init?: EndpointRequestInit): Promise<InheritedEnum | undefined> {
  return client.call('ExistingPropertyEndpoint', 'sendInheritedEnum', {}, init);
}

export { send, sendEnumDiscriminated, sendInherited, sendInheritedEnum };
