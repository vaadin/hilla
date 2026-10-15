/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
import { assert } from '@esm-bundle/chai';
import type { VaadinWindow } from './types.js';

describe('@vaadin/hilla-lit-form', () => {
  describe('Index', () => {
    const $wnd = window as VaadinWindow;

    it('should add registration', () => {
      assert.isDefined($wnd.Vaadin);
      assert.isArray($wnd.Vaadin.registrations);
      const formRegistrations = $wnd.Vaadin.registrations!.filter((r) => r.is === '@vaadin/hilla-lit-form');
      assert.lengthOf(formRegistrations, 1);
    });
  });
});
