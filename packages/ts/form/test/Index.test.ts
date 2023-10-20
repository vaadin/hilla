/*
 * Copyright 2000-2020 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import { assert } from '@esm-bundle/chai';
import type { VaadinWindow } from './types.js';

describe('@hilla/form', () => {
  describe('Index', () => {
    const $wnd = window as VaadinWindow;

    it('should add registration', () => {
      assert.isDefined($wnd.Vaadin);
      assert.isArray($wnd.Vaadin!.registrations);
      const formRegistrations = $wnd.Vaadin!.registrations!.filter((r) => r.is === '@hilla/form');
      assert.lengthOf(formRegistrations, 1);
    });
  });
});
