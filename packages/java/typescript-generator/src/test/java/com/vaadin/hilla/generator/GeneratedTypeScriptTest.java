/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.hilla.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.vaadin.hilla.generator.fixtures.EmptyEndpoint;
import com.vaadin.hilla.generator.fixtures.InheritingEndpoint;
import com.vaadin.hilla.generator.fixtures.SampleEndpoint;
import com.vaadin.hilla.generator.fixtures.ShadowingEndpoint;
import com.vaadin.hilla.generator.model.EndpointModel;
import com.vaadin.hilla.generator.model.EntityModel;
import com.vaadin.hilla.generator.model.UnionModel;
import com.vaadin.hilla.generator.typescript.BarrelWriter;
import com.vaadin.hilla.generator.typescript.ClientWriter;
import com.vaadin.hilla.generator.typescript.EndpointWriter;
import com.vaadin.hilla.generator.typescript.EntityWriter;
import com.vaadin.hilla.generator.typescript.FormModelWriter;
import com.vaadin.hilla.generator.typescript.GeneratedFile;
import com.vaadin.hilla.generator.typescript.UnionWriter;
import com.vaadin.hilla.parser.testutils.FullStackGenerator;

/**
 * Verifies the TypeScript written for a browser callable class, all the way
 * from the class itself.
 */
public class GeneratedTypeScriptTest {
    private static final List<EndpointModel> endpoints = endpointsOf(
            SampleEndpoint.class);
    private static final List<EntityModel> entities = new FullStackGenerator(
            GeneratedTypeScriptTest.class, SampleEndpoint.class)
            .parseEntities();
    private static final List<UnionModel> unions = new FullStackGenerator(
            GeneratedTypeScriptTest.class, SampleEndpoint.class).parseUnions();

    @Test
    public void should_WriteTheEndpoint() {
        var file = new EndpointWriter(ClientWriter.MODULE_SPECIFIER)
                .write(endpoints.get(0));

        assertEquals("SampleEndpoint.ts", file.path());
        assertEquals(
                """
                        import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
                        import type Bounded from './com/vaadin/hilla/generator/fixtures/SampleEndpoint/Bounded.js';
                        import type Detailed from './com/vaadin/hilla/generator/fixtures/SampleEndpoint/Detailed.js';
                        import type Figure from './com/vaadin/hilla/generator/fixtures/SampleEndpoint/Figure.js';
                        import type Kind from './com/vaadin/hilla/generator/fixtures/SampleEndpoint/Kind.js';
                        import type Marker from './com/vaadin/hilla/generator/fixtures/SampleEndpoint/Marker.js';
                        import type Mixed from './com/vaadin/hilla/generator/fixtures/SampleEndpoint/Mixed.js';
                        import type Sample from './com/vaadin/hilla/generator/fixtures/SampleEndpoint/Sample.js';
                        import type Wrapper from './com/vaadin/hilla/generator/fixtures/SampleEndpoint/Wrapper.js';
                        import client from './connect-client.default.js';

                        export async function all(init?: EndpointRequestInit): Promise<Array<Sample | undefined> | undefined> {
                          return client.call('SampleEndpoint', 'all', {}, init);
                        }

                        export async function bounded(init?: EndpointRequestInit): Promise<Bounded | undefined> {
                          return client.call('SampleEndpoint', 'bounded', {}, init);
                        }

                        export async function count(init?: EndpointRequestInit): Promise<number> {
                          return client.call('SampleEndpoint', 'count', {}, init);
                        }

                        export async function counts(init?: EndpointRequestInit): Promise<Record<string, number | undefined> | undefined> {
                          return client.call('SampleEndpoint', 'counts', {}, init);
                        }

                        export async function describe(
                          firstName: string | undefined,
                          lastName: string | undefined,
                          age: number,
                          init?: EndpointRequestInit,
                        ): Promise<string | undefined> {
                          return client.call('SampleEndpoint', 'describe', { firstName, lastName, age }, init);
                        }

                        export async function detailed(init?: EndpointRequestInit): Promise<Detailed | undefined> {
                          return client.call('SampleEndpoint', 'detailed', {}, init);
                        }

                        export async function figure(init?: EndpointRequestInit): Promise<Figure | undefined> {
                          return client.call('SampleEndpoint', 'figure', {}, init);
                        }

                        export async function find(id: string | undefined, init?: EndpointRequestInit): Promise<Sample | undefined> {
                          return client.call('SampleEndpoint', 'find', { id }, init);
                        }

                        export async function greet(name: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> {
                          return client.call('SampleEndpoint', 'greet', { name }, init);
                        }

                        export async function kind(init?: EndpointRequestInit): Promise<Kind | undefined> {
                          return client.call('SampleEndpoint', 'kind', {}, init);
                        }

                        export async function marker(init?: EndpointRequestInit): Promise<Marker | undefined> {
                          return client.call('SampleEndpoint', 'marker', {}, init);
                        }

                        export async function maybe(init?: EndpointRequestInit): Promise<string | undefined> {
                          return client.call('SampleEndpoint', 'maybe', {}, init);
                        }

                        export async function maybeCounts(init?: EndpointRequestInit): Promise<Record<string, number | undefined> | undefined> {
                          return client.call('SampleEndpoint', 'maybeCounts', {}, init);
                        }

                        export async function maybeNames(init?: EndpointRequestInit): Promise<Array<string | undefined> | undefined> {
                          return client.call('SampleEndpoint', 'maybeNames', {}, init);
                        }

                        export async function mixed(init?: EndpointRequestInit): Promise<Mixed | undefined> {
                          return client.call('SampleEndpoint', 'mixed', {}, init);
                        }

                        export async function names(init?: EndpointRequestInit): Promise<Array<string | undefined> | undefined> {
                          return client.call('SampleEndpoint', 'names', {}, init);
                        }

                        export async function ping(init?: EndpointRequestInit): Promise<void> {
                          return client.call('SampleEndpoint', 'ping', {}, init);
                        }

                        export async function required(name: string, init?: EndpointRequestInit): Promise<string> {
                          return client.call('SampleEndpoint', 'required', { name }, init);
                        }

                        export async function wrapped(init?: EndpointRequestInit): Promise<Wrapper<Sample | undefined> | undefined> {
                          return client.call('SampleEndpoint', 'wrapped', {}, init);
                        }
                        """,
                file.content());
    }

    @Test
    public void should_NotLetAParameterShadowWhatTheFileNeedsForItself() {
        var endpoint = endpointsOf(ShadowingEndpoint.class).get(0);

        assertEquals(
                """
                        import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
                        import client_1 from './connect-client.default.js';

                        export async function client(init?: EndpointRequestInit): Promise<string | undefined> {
                          return client_1.call('ShadowingEndpoint', 'client', {}, init);
                        }

                        export async function echo(
                          client: string | undefined,
                          init: string | undefined,
                          _init?: EndpointRequestInit,
                        ): Promise<string | undefined> {
                          return client_1.call('ShadowingEndpoint', 'echo', { client, init }, _init);
                        }
                        """,
                new EndpointWriter(ClientWriter.MODULE_SPECIFIER)
                        .write(endpoint).content());
    }

    /**
     * The Node generator writes no file at all for such an endpoint, and no
     * entry in the barrel either. Writing a module which exports nothing keeps
     * the barrel able to name every endpoint; whether the file is worth writing
     * is for the step which puts these writers in the pipeline.
     */
    @Test
    public void should_WriteAModuleForAnEndpointWithoutMethods() {
        var endpoint = endpointsOf(EmptyEndpoint.class).get(0);

        assertEquals(List.of(), endpoint.methods());
        assertEquals("export {};\n",
                new EndpointWriter(ClientWriter.MODULE_SPECIFIER)
                        .write(endpoint).content());
    }

    @Test
    public void should_WriteAMethodOnceAlthoughTheWalkCarriesItTwice() {
        var endpoint = endpointsOf(InheritingEndpoint.class).get(0);

        assertEquals(
                """
                        import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
                        import client from './connect-client.default.js';

                        export async function shared(init?: EndpointRequestInit): Promise<string | undefined> {
                          return client.call('InheritingEndpoint', 'shared', {}, init);
                        }

                        export async function twice(one: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> {
                          return client.call('InheritingEndpoint', 'twice', { one }, init);
                        }
                        """,
                new EndpointWriter(ClientWriter.MODULE_SPECIFIER)
                        .write(endpoint).content());
    }

    @Test
    public void should_WriteTheClient() {
        assertEquals("""
                import { ConnectClient } from '@vaadin/hilla-frontend';

                const client = new ConnectClient({ prefix: 'connect' });

                export default client;
                """, new ClientWriter().write().content());
    }

    @Test
    public void should_WriteAnEmptyBarrelWithoutEndpoints() {
        assertEquals("export {};\n",
                new BarrelWriter().write(List.of()).content());
    }

    @Test
    public void should_WriteTheBarrel() {
        assertEquals("""
                import * as SampleEndpoint from './SampleEndpoint.js';

                export { SampleEndpoint };
                """, new BarrelWriter().write(endpoints).content());
    }

    @Test
    public void should_ReExportEveryEndpointInTheSameOrderWhicheverItIsGiven() {
        // Given the other way around than they are written, so that the order
        // is the one the barrel decides rather than the one it was handed
        var written = new BarrelWriter().write(List.of(
                endpointsOf(ShadowingEndpoint.class).get(0), endpoints.get(0)));

        assertEquals("""
                import * as SampleEndpoint from './SampleEndpoint.js';
                import * as ShadowingEndpoint from './ShadowingEndpoint.js';

                export { SampleEndpoint, ShadowingEndpoint };
                """, written.content());
    }

    @Test
    public void should_DescribeOnlyTheLastRunOfTheParser() {
        var generator = new FullStackGenerator(GeneratedTypeScriptTest.class,
                SampleEndpoint.class);
        generator.parseModel();

        assertEquals(1, generator.parseModel().size());
    }

    @Test
    public void should_WriteAnEntity() {
        var file = write(entity(SampleEndpoint.Sample.class));

        assertEquals(
                "com/vaadin/hilla/generator/fixtures/SampleEndpoint/Sample.ts",
                file.path());
        assertEquals("""
                import type Sample_1 from './Sample.js';

                interface Sample {
                  name?: string;
                  parent?: Sample_1;
                  label?: string;
                }

                export default Sample;
                """, file.content());
    }

    @Test
    public void should_WriteAnEntityExtendingAnother() {
        assertEquals("""
                import type Sample from './Sample.js';

                interface Detailed extends Sample {
                  note?: string;
                }

                export default Detailed;
                """, write(entity(SampleEndpoint.Detailed.class)).content());
    }

    @Test
    public void should_WriteAGenericEntity() {
        // The type parameter defaults to an unknown type, so that the
        // declaration can also be referred to without saying what it holds
        assertEquals("""
                interface Wrapper<T = unknown> {
                  value?: T;
                }

                export default Wrapper;
                """, write(entity(SampleEndpoint.Wrapper.class)).content());
    }

    @Test
    public void should_WriteWhatATypeParameterIsBoundToInsteadOfItsName() {
        // The declaration does not keep a parameter bound to something else
        // than an object, so nothing would declare the name it goes by
        assertEquals("""
                import type Sample from './Sample.js';

                interface Bounded {
                  held?: Sample;
                }

                export default Bounded;
                """, write(entity(SampleEndpoint.Bounded.class)).content());
    }

    @Test
    public void should_WriteAnEntityWithoutProperties() {
        assertEquals("""
                interface Marker {}

                export default Marker;
                """, write(entity(SampleEndpoint.Marker.class)).content());
    }

    @Test
    public void should_WriteAnEnumAsTheValuesItIsSerializedAs() {
        assertEquals("""
                enum Kind {
                  ONE = 'ONE',
                  OTHER = 'OTHER',
                }

                export default Kind;
                """, write(entity(SampleEndpoint.Kind.class)).content());
    }

    @Test
    public void should_WriteTheModelOfAnEntity() {
        var file = writeModel(entity(SampleEndpoint.Sample.class));

        assertEquals(
                "com/vaadin/hilla/generator/fixtures/SampleEndpoint/SampleModel.ts",
                file.path());
        assertEquals(
                """
                        import { ObjectModel, StringModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
                        import type Sample from './Sample.js';
                        import SampleModel_1 from './SampleModel.js';

                        class SampleModel<T extends Sample = Sample> extends ObjectModel<T> {
                          static override createEmptyValue = makeObjectEmptyValueCreator(SampleModel);

                          get name(): StringModel {
                            return this[_getPropertyModel]('name', (parent, key) =>
                              new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
                          }

                          get parent(): SampleModel_1 {
                            return this[_getPropertyModel]('parent', (parent, key) =>
                              new SampleModel_1(parent, key, true));
                          }

                          get label(): StringModel {
                            return this[_getPropertyModel]('label', (parent, key) =>
                              new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
                          }
                        }

                        export default SampleModel;
                        """,
                file.content());
    }

    @Test
    public void should_WriteTheModelOfEveryKindOfProperty() {
        // The model of a value is the model of the type it holds, which is one
        // of the form library for everything but a generated type
        var file = writeModel(entity(SampleEndpoint.Mixed.class));

        assertEquals(
                "com/vaadin/hilla/generator/fixtures/SampleEndpoint/MixedModel.ts",
                file.path());
        assertEquals(
                """
                        import {
                          ArrayModel,
                          BooleanModel,
                          NumberModel,
                          ObjectModel,
                          StringModel,
                          _getPropertyModel,
                          makeObjectEmptyValueCreator,
                        } from '@vaadin/hilla-lit-form';
                        import KindModel from './KindModel.js';
                        import type Mixed from './Mixed.js';
                        import SampleModel from './SampleModel.js';

                        class MixedModel<T extends Mixed = Mixed> extends ObjectModel<T> {
                          static override createEmptyValue = makeObjectEmptyValueCreator(MixedModel);

                          get count(): NumberModel {
                            return this[_getPropertyModel]('count', (parent, key) =>
                              new NumberModel(parent, key, false, { meta: { javaType: 'int' } }));
                          }

                          get active(): BooleanModel {
                            return this[_getPropertyModel]('active', (parent, key) =>
                              new BooleanModel(parent, key, false, { meta: { javaType: 'boolean' } }));
                          }

                          get tags(): ArrayModel<StringModel> {
                            return this[_getPropertyModel]('tags', (parent, key) =>
                              new ArrayModel(parent, key, true,
                                (parent, key) => new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }),
                                { meta: { javaType: 'java.util.List' } }));
                          }

                          get counts(): ObjectModel<Record<string, number | undefined>> {
                            return this[_getPropertyModel]('counts', (parent, key) =>
                              new ObjectModel(parent, key, true, { meta: { javaType: 'java.util.Map' } }));
                          }

                          get kind(): KindModel {
                            return this[_getPropertyModel]('kind', (parent, key) =>
                              new KindModel(parent, key, true));
                          }

                          get sample(): SampleModel {
                            return this[_getPropertyModel]('sample', (parent, key) =>
                              new SampleModel(parent, key, true));
                          }

                          get words(): ArrayModel<StringModel> {
                            return this[_getPropertyModel]('words', (parent, key) =>
                              new ArrayModel(parent, key, true,
                                (parent, key) => new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }),
                                { meta: { javaType: 'java.lang.String[]' } }));
                          }

                          get unique(): ArrayModel<StringModel> {
                            return this[_getPropertyModel]('unique', (parent, key) =>
                              new ArrayModel(parent, key, true,
                                (parent, key) => new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }),
                                { meta: { javaType: 'java.util.Set' } }));
                          }

                          get grouped(): ObjectModel<Record<string, ReadonlyArray<string | undefined> | undefined>> {
                            return this[_getPropertyModel]('grouped', (parent, key) =>
                              new ObjectModel(parent, key, true, { meta: { javaType: 'java.util.Map' } }));
                          }

                          get maybeTags(): ArrayModel<StringModel> {
                            return this[_getPropertyModel]('maybeTags', (parent, key) =>
                              new ArrayModel(parent, key, true,
                                (parent, key) => new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }),
                                { meta: { javaType: 'java.util.List' } }));
                          }
                        }

                        export default MixedModel;
                        """,
                file.content());
    }

    @Test
    public void should_WriteTheModelOfATypeInheritingProperties() {
        // Built on the model of the type the properties come from, so that the
        // inherited ones are bound as well
        var file = writeModel(entity(SampleEndpoint.Detailed.class));

        assertEquals(
                "com/vaadin/hilla/generator/fixtures/SampleEndpoint/DetailedModel.ts",
                file.path());
        assertEquals(
                """
                        import { StringModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
                        import type Detailed from './Detailed.js';
                        import SampleModel from './SampleModel.js';

                        class DetailedModel<T extends Detailed = Detailed> extends SampleModel<T> {
                          static override createEmptyValue = makeObjectEmptyValueCreator(DetailedModel);

                          get note(): StringModel {
                            return this[_getPropertyModel]('note', (parent, key) =>
                              new StringModel(parent, key, true, { meta: { javaType: 'java.lang.String' } }));
                          }
                        }

                        export default DetailedModel;
                        """,
                file.content());
    }

    @Test
    public void should_WriteTheModelOfAGenericEntity() {
        var file = writeModel(entity(SampleEndpoint.Wrapper.class));

        assertEquals(
                "com/vaadin/hilla/generator/fixtures/SampleEndpoint/WrapperModel.ts",
                file.path());
        assertEquals(
                """
                        import { ObjectModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
                        import type Wrapper from './Wrapper.js';

                        class WrapperModel<T extends Wrapper = Wrapper> extends ObjectModel<T> {
                          static override createEmptyValue = makeObjectEmptyValueCreator(WrapperModel);

                          get value(): ObjectModel {
                            return this[_getPropertyModel]('value', (parent, key) =>
                              new ObjectModel(parent, key, true));
                          }
                        }

                        export default WrapperModel;
                        """,
                file.content());
    }

    @Test
    public void should_WriteTheModelOfAnEntityWithoutProperties() {
        var file = writeModel(entity(SampleEndpoint.Marker.class));

        assertEquals(
                "com/vaadin/hilla/generator/fixtures/SampleEndpoint/MarkerModel.ts",
                file.path());
        assertEquals(
                """
                        import { ObjectModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
                        import type Marker from './Marker.js';

                        class MarkerModel<T extends Marker = Marker> extends ObjectModel<T> {
                          static override createEmptyValue = makeObjectEmptyValueCreator(MarkerModel);
                        }

                        export default MarkerModel;
                        """,
                file.content());
    }

    @Test
    public void should_WriteTheModelOfAnEnum() {
        // An enum is bound as the constants it accepts rather than as
        // properties
        var file = writeModel(entity(SampleEndpoint.Kind.class));

        assertEquals(
                "com/vaadin/hilla/generator/fixtures/SampleEndpoint/KindModel.ts",
                file.path());
        assertEquals(
                """
                        import { EnumModel, _enum, makeEnumEmptyValueCreator } from '@vaadin/hilla-lit-form';
                        import Kind from './Kind.js';

                        class KindModel extends EnumModel<typeof Kind> {
                          static override createEmptyValue = makeEnumEmptyValueCreator(KindModel);

                          readonly [_enum] = Kind;
                        }

                        export default KindModel;
                        """,
                file.content());
    }

    @Test
    public void should_WriteWhichSubtypeAValueOfAPolymorphicTypeCanBe() {
        // A subtype accepting the discriminator of the one below it is
        // narrowed to its own value, or a value of it would be a value of both
        var file = new UnionWriter().write(unions.get(0));

        assertEquals(
                "com/vaadin/hilla/generator/fixtures/SampleEndpoint/FigureUnion.ts",
                file.path());
        assertEquals("""
                import type Ring from './Figure/Ring.js';
                import type Round from './Figure/Round.js';

                type FigureUnion = (Round & { figure: 'round' }) | Ring;

                export default FigureUnion;
                """, file.content());
    }

    @Test
    public void should_WriteTheDiscriminatorOfASubtypeAsTheValuesItAccepts() {
        assertEquals("""
                import type Figure from '../Figure.js';

                interface Round extends Figure {
                  radius: number;
                  figure: 'round' | 'ring';
                }

                export default Round;
                """,
                write(entity(SampleEndpoint.Figure.Round.class)).content());
    }

    @Test
    public void should_LeaveTheDiscriminatorOutOfTheModelOfASubtype() {
        // A form binds what the type declares, and the discriminator is
        // decided by which subtype the value is rather than edited
        assertEquals(
                """
                        import { NumberModel, _getPropertyModel, makeObjectEmptyValueCreator } from '@vaadin/hilla-lit-form';
                        import FigureModel from '../FigureModel.js';
                        import type Round from './Round.js';

                        class RoundModel<T extends Round = Round> extends FigureModel<T> {
                          static override createEmptyValue = makeObjectEmptyValueCreator(RoundModel);

                          get radius(): NumberModel {
                            return this[_getPropertyModel]('radius', (parent, key) =>
                              new NumberModel(parent, key, false, { meta: { javaType: 'double' } }));
                          }
                        }

                        export default RoundModel;
                        """,
                writeModel(entity(SampleEndpoint.Figure.Round.class))
                        .content());
    }

    private static GeneratedFile write(EntityModel entity) {
        return new EntityWriter().write(entity);
    }

    private static GeneratedFile writeModel(EntityModel entity) {
        return new FormModelWriter().write(entity);
    }

    private static EntityModel entity(Class<?> javaClass) {
        return entities.stream().filter(
                entity -> entity.javaClass().equals(javaClass.getName()))
                .findFirst().orElseThrow(() -> new AssertionError(
                        "Nothing was generated for " + javaClass));
    }

    private static List<EndpointModel> endpointsOf(Class<?> endpoint) {
        return new FullStackGenerator(GeneratedTypeScriptTest.class, endpoint)
                .parseModel();
    }
}
