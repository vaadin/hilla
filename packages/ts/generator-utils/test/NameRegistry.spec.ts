import { beforeEach, describe, expect, it } from 'vitest';
import NameRegistry from '../src/dependencies/NameRegistry.js';

describe('NameRegistry', () => {
  let registry: NameRegistry;

  beforeEach(() => {
    registry = new NameRegistry();
  });

  it('should hand out a free name as it is', () => {
    expect(registry.reserve('Model')).to.equal('Model');
  });

  it('should count up until the suffixed name is free', () => {
    expect(registry.reserve('Model')).to.equal('Model');
    expect(registry.reserve('Model')).to.equal('Model_1');
    expect(registry.reserve('Model')).to.equal('Model_2');
  });

  it('should suffix a name taken by a claim', () => {
    registry.claim('Model');

    expect(registry.reserve('Model')).to.equal('Model_1');
  });

  it('should suffix a reserved word, as it cannot be a binding name', () => {
    expect(registry.reserve('delete')).to.equal('delete_1');
    expect(registry.reserve('class')).to.equal('class_1');
    expect(registry.reserve('await')).to.equal('await_1');
    expect(registry.reserve('eval')).to.equal('eval_1');
    expect(registry.reserve('arguments')).to.equal('arguments_1');
  });

  it('should leave a contextual keyword alone, as it is a valid identifier', () => {
    expect(registry.reserve('type')).to.equal('type');
    expect(registry.reserve('get')).to.equal('get');
    expect(registry.reserve('string')).to.equal('string');
  });

  it('should create an identifier out of the reserved name', () => {
    registry.claim('Model');

    expect(registry.reserveIdentifier('Model').text).to.equal('Model_1');
  });
});
