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
import com.vaadin.hilla.generator.fixtures.ProvidedTypesEndpoint;
import com.vaadin.hilla.generator.fixtures.PushingEndpoint;
import com.vaadin.hilla.generator.fixtures.SampleEndpoint;
import com.vaadin.hilla.generator.fixtures.ShadowingEndpoint;
import com.vaadin.hilla.generator.fixtures.ValidatedEndpoint;
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
                        import type { EndpointRequestInit, Subscription } from '@vaadin/hilla-frontend';
                        import type Bounded from './com/vaadin/hilla/generator/fixtures/SampleEndpoint/Bounded.js';
                        import type Detailed from './com/vaadin/hilla/generator/fixtures/SampleEndpoint/Detailed.js';
                        import type Figure from './com/vaadin/hilla/generator/fixtures/SampleEndpoint/Figure.js';
                        import type Kind from './com/vaadin/hilla/generator/fixtures/SampleEndpoint/Kind.js';
                        import type Marker from './com/vaadin/hilla/generator/fixtures/SampleEndpoint/Marker.js';
                        import type Mixed from './com/vaadin/hilla/generator/fixtures/SampleEndpoint/Mixed.js';
                        import type Sample from './com/vaadin/hilla/generator/fixtures/SampleEndpoint/Sample.js';
                        import type Shaded from './com/vaadin/hilla/generator/fixtures/SampleEndpoint/Shaded.js';
                        import type Wrapper from './com/vaadin/hilla/generator/fixtures/SampleEndpoint/Wrapper.js';
                        import client from './connect-client.default.js';

                        async function all(init?: EndpointRequestInit): Promise<Array<Sample | undefined> | undefined> {
                          return client.call('SampleEndpoint', 'all', {}, init);
                        }

                        async function bounded(init?: EndpointRequestInit): Promise<Bounded | undefined> {
                          return client.call('SampleEndpoint', 'bounded', {}, init);
                        }

                        async function count(init?: EndpointRequestInit): Promise<number> {
                          return client.call('SampleEndpoint', 'count', {}, init);
                        }

                        async function counts(init?: EndpointRequestInit): Promise<Record<string, number | undefined> | undefined> {
                          return client.call('SampleEndpoint', 'counts', {}, init);
                        }

                        async function describe(firstName: string | undefined, lastName: string | undefined, age: number, init?: EndpointRequestInit): Promise<string | undefined> {
                          return client.call('SampleEndpoint', 'describe', { firstName, lastName, age }, init);
                        }

                        async function detailed(init?: EndpointRequestInit): Promise<Detailed | undefined> {
                          return client.call('SampleEndpoint', 'detailed', {}, init);
                        }

                        async function figure(init?: EndpointRequestInit): Promise<Figure | undefined> {
                          return client.call('SampleEndpoint', 'figure', {}, init);
                        }

                        async function find(id: string | undefined, init?: EndpointRequestInit): Promise<Sample | undefined> {
                          return client.call('SampleEndpoint', 'find', { id }, init);
                        }

                        async function greet(name: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> {
                          return client.call('SampleEndpoint', 'greet', { name }, init);
                        }

                        async function kind(init?: EndpointRequestInit): Promise<Kind | undefined> {
                          return client.call('SampleEndpoint', 'kind', {}, init);
                        }

                        async function marker(init?: EndpointRequestInit): Promise<Marker | undefined> {
                          return client.call('SampleEndpoint', 'marker', {}, init);
                        }

                        async function maybe(init?: EndpointRequestInit): Promise<string | undefined> {
                          return client.call('SampleEndpoint', 'maybe', {}, init);
                        }

                        async function maybeCounts(init?: EndpointRequestInit): Promise<Record<string, number | undefined> | undefined> {
                          return client.call('SampleEndpoint', 'maybeCounts', {}, init);
                        }

                        async function maybeNames(init?: EndpointRequestInit): Promise<Array<string | undefined> | undefined> {
                          return client.call('SampleEndpoint', 'maybeNames', {}, init);
                        }

                        async function mixed(init?: EndpointRequestInit): Promise<Mixed | undefined> {
                          return client.call('SampleEndpoint', 'mixed', {}, init);
                        }

                        async function names(init?: EndpointRequestInit): Promise<Array<string | undefined> | undefined> {
                          return client.call('SampleEndpoint', 'names', {}, init);
                        }

                        async function ping(init?: EndpointRequestInit): Promise<void> {
                          return client.call('SampleEndpoint', 'ping', {}, init);
                        }

                        async function required(name: string, init?: EndpointRequestInit): Promise<string> {
                          return client.call('SampleEndpoint', 'required', { name }, init);
                        }

                        async function shaded(init?: EndpointRequestInit): Promise<Shaded | undefined> {
                          return client.call('SampleEndpoint', 'shaded', {}, init);
                        }

                        function stream(count: number): Subscription<string | undefined> {
                          return client.subscribe('SampleEndpoint', 'stream', { count });
                        }

                        function watch(): Subscription<Sample | undefined> {
                          return client.subscribe('SampleEndpoint', 'watch', {});
                        }

                        async function wrapped(init?: EndpointRequestInit): Promise<Wrapper<Sample | undefined> | undefined> {
                          return client.call('SampleEndpoint', 'wrapped', {}, init);
                        }

                        export { all, bounded, count, counts, describe, detailed, figure, find, greet, kind, marker, maybe, maybeCounts, maybeNames, mixed, names, ping, required, shaded, stream, watch, wrapped };
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

                        async function client(init?: EndpointRequestInit): Promise<string | undefined> {
                          return client_1.call('ShadowingEndpoint', 'client', {}, init);
                        }

                        async function echo(client: string | undefined, init: string | undefined, _init?: EndpointRequestInit): Promise<string | undefined> {
                          return client_1.call('ShadowingEndpoint', 'echo', { client, init }, _init);
                        }

                        export { client, echo };
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
    public void should_TakeNoRequestOptionsWhenEveryMethodSendsASeries() {
        // The options are those of a single request, so a file with nothing
        // but subscriptions in it has no use for the type of them either. A
        // value of the series which cannot be absent is not written as one
        // which can
        var endpoint = endpointsOf(PushingEndpoint.class).get(0);

        assertEquals(
                """
                        import type { Subscription } from '@vaadin/hilla-frontend';
                        import client from './connect-client.default.js';

                        function messages(): Subscription<string | undefined> {
                          return client.subscribe('PushingEndpoint', 'messages', {});
                        }

                        function nonNullMessages(): Subscription<string> {
                          return client.subscribe('PushingEndpoint', 'nonNullMessages', {});
                        }

                        export { messages, nonNullMessages };
                        """,
                new EndpointWriter(ClientWriter.MODULE_SPECIFIER)
                        .write(endpoint).content());
    }

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

                        async function shared(init?: EndpointRequestInit): Promise<string | undefined> {
                          return client.call('InheritingEndpoint', 'shared', {}, init);
                        }

                        async function twice(one: string | undefined, init?: EndpointRequestInit): Promise<string | undefined> {
                          return client.call('InheritingEndpoint', 'twice', { one }, init);
                        }

                        export { shared, twice };
                        """,
                new EndpointWriter(ClientWriter.MODULE_SPECIFIER)
                        .write(endpoint).content());
    }

    @Test
    public void should_ReferToATypeItDoesNotDeclareByName() {
        // The browser has a file, and a module of the framework exports the
        // signals, so nothing is generated for either of them
        var generator = new FullStackGenerator(GeneratedTypeScriptTest.class,
                ProvidedTypesEndpoint.class);

        assertEquals(
                """
                        import type { EndpointRequestInit } from '@vaadin/hilla-frontend';
                        import type { Signal } from '@vaadin/hilla-react-signals';
                        import client from './connect-client.default.js';

                        async function signal(init?: EndpointRequestInit): Promise<Signal<string | undefined> | undefined> {
                          return client.call('ProvidedTypesEndpoint', 'signal', {}, init);
                        }

                        async function upload(file: File | undefined, init?: EndpointRequestInit): Promise<void> {
                          return client.call('ProvidedTypesEndpoint', 'upload', { file }, init);
                        }

                        export { signal, upload };
                        """,
                new EndpointWriter(ClientWriter.MODULE_SPECIFIER)
                        .write(generator.parseModel().get(0)).content());
        assertEquals(List.of(), generator.parseEntities());
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
    public void should_ExportAnEndpointUnderItsOwnName() {
        var written = new BarrelWriter().write(List.of(
                new EndpointModel("Record", "com.example.Record", List.of())));

        assertEquals("""
                import * as Record_1 from './Record.js';

                export { Record_1 as Record };
                """, written.content());
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
    public void should_WriteAPropertyOfEveryKind() {
        // A property of a type which is not generated is referred to by name,
        // as everywhere else, and the model of it is the one of an object: the
        // module exporting the type is what makes a value of it
        assertEquals(
                """
                        import type { Signal } from '@vaadin/hilla-react-signals';
                        import type Kind from './Kind.js';
                        import type Sample from './Sample.js';

                        interface Mixed {
                          count: number;
                          active: boolean;
                          tags?: Array<string | undefined>;
                          counts?: Record<string, number | undefined>;
                          kind?: Kind;
                          sample?: Sample;
                          words?: Array<string | undefined>;
                          unique?: Array<string | undefined>;
                          grouped?: Record<string, Array<string | undefined> | undefined>;
                          maybeTags?: Array<string | undefined>;
                          signal?: Signal<string | undefined>;
                        }

                        export default Mixed;
                        """,
                write(entity(SampleEndpoint.Mixed.class)).content());
    }

    @Test
    public void should_WriteWhatATypeParameterIsBoundToInsteadOfItsName() {
        // The declaration does not keep a parameter bound to something else
        // than an object, so nothing would declare the name it goes by. Of
        // more than one bound, the first is written, being the one the
        // parameter erases to
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
                interface Marker {
                }

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
                        import m, { StringModel } from '@vaadin/hilla-models';
                        import type Sample from './Sample.js';

                        const SampleModel = m
                          .object<Sample>('Sample')
                          .property('name', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
                          .property('parent', m.optional(m.self))
                          .property('label', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
                          .build();
                        type SampleModel = typeof SampleModel;

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
                        import m, { BooleanModel, NumberModel, ObjectModel, StringModel } from '@vaadin/hilla-models';
                        import KindModel from './KindModel.js';
                        import type Mixed from './Mixed.js';
                        import SampleModel from './SampleModel.js';

                        const MixedModel = m
                          .object<Mixed>('Mixed')
                          .property('count', m.meta(NumberModel, { jvmType: 'int' }))
                          .property('active', m.meta(BooleanModel, { jvmType: 'boolean' }))
                          .property('tags', m.meta(m.optional(m.array(m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))), { jvmType: 'java.util.List' }))
                          .property('counts', m.meta(m.optional(m.record(m.meta(m.optional(NumberModel), { jvmType: 'java.lang.Integer' }))), { jvmType: 'java.util.Map' }))
                          .property('kind', m.optional(KindModel))
                          .property('sample', m.optional(SampleModel))
                          .property('words', m.meta(m.optional(m.array(m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))), { jvmType: 'java.lang.String[]' }))
                          .property('unique', m.meta(m.optional(m.array(m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))), { jvmType: 'java.util.Set' }))
                          .property('grouped', m.meta(m.optional(m.record(m.meta(m.optional(m.array(m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))), { jvmType: 'java.util.List' }))), { jvmType: 'java.util.Map' }))
                          .property('maybeTags', m.meta(m.optional(m.array(m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))), { jvmType: 'java.util.List' }))
                          .property('signal', m.optional(ObjectModel))
                          .build();
                        type MixedModel = typeof MixedModel;

                        export default MixedModel;
                        """,
                file.content());
    }

    @Test
    public void should_BindWhatTheValuesOfATypeHaveToSatisfy() {
        // The constraints are named after the annotations, and are given what
        // the annotation says: the one value it says when that is all of it,
        // and everything else the constraint declares otherwise, which leaves
        // out the flags of a pattern. The values of a collection are
        // constrained on their own, as the collection is. What a value is to
        // the persistence API is written along with its Java type, which is
        // how a grid tells an id from a value worth showing
        assertEquals(
                """
                        import m, { Email, Min, NotBlank, NumberModel, Pattern, Size, StringModel } from '@vaadin/hilla-models';
                        import type Validated from './Validated.js';

                        const ValidatedModel = m
                          .object<Validated>('Validated')
                          .property('id', m.meta(m.optional(NumberModel), { annotations: [{ jvmType: 'jakarta.persistence.Id' }], jvmType: 'long' }))
                          .property('name', m.meta(m.constrained(m.optional(StringModel), NotBlank()), { jvmType: 'java.lang.String' }))
                          .property('count', m.meta(m.constrained(NumberModel, Min(1)), { jvmType: 'int' }))
                          .property('address', m.meta(m.constrained(m.optional(StringModel), Email({ message: 'that isn\\'t an address' })), { jvmType: 'java.lang.String' }))
                          .property('code', m.meta(m.constrained(m.optional(StringModel), Pattern({ regexp: '[a-z]+' })), { jvmType: 'java.lang.String' }))
                          .property('codes', m.meta(m.optional(m.array(m.meta(m.constrained(m.optional(StringModel), Size({ min: 1 })), { jvmType: 'java.lang.String' }))), { jvmType: 'java.lang.String[]' }))
                          .property('tags', m.meta(m.constrained(m.optional(m.array(m.meta(m.constrained(m.optional(StringModel), NotBlank()), { jvmType: 'java.lang.String' }))), Size({ max: 10, min: 1 })), { jvmType: 'java.util.List' }))
                          .build();
                        type ValidatedModel = typeof ValidatedModel;

                        export default ValidatedModel;
                        """,
                new FormModelWriter(List.of(validated())).write(validated())
                        .content());
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
                        import m, { StringModel } from '@vaadin/hilla-models';
                        import type Detailed from './Detailed.js';
                        import SampleModel from './SampleModel.js';

                        const DetailedModel = m
                          .extend(SampleModel)
                          .object<Detailed>('Detailed')
                          .property('note', m.meta(m.optional(StringModel), { jvmType: 'java.lang.String' }))
                          .build();
                        type DetailedModel = typeof DetailedModel;

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
        assertEquals("""
                import m, { Model } from '@vaadin/hilla-models';
                import type Wrapper from './Wrapper.js';

                const WrapperModel = m
                  .object<Wrapper>('Wrapper')
                  .property('value', m.optional(Model))
                  .build();
                type WrapperModel = typeof WrapperModel;

                export default WrapperModel;
                """, file.content());
    }

    @Test
    public void should_WriteTheModelOfAnEntityWithoutProperties() {
        var file = writeModel(entity(SampleEndpoint.Marker.class));

        assertEquals(
                "com/vaadin/hilla/generator/fixtures/SampleEndpoint/MarkerModel.ts",
                file.path());
        assertEquals("""
                import m from '@vaadin/hilla-models';
                import type Marker from './Marker.js';

                const MarkerModel = m
                  .object<Marker>('Marker')
                  .build();
                type MarkerModel = typeof MarkerModel;

                export default MarkerModel;
                """, file.content());
    }

    @Test
    public void should_WriteTheModelOfAnEnum() {
        // An enum is bound as the constants it accepts rather than as
        // properties
        var file = writeModel(entity(SampleEndpoint.Kind.class));

        assertEquals(
                "com/vaadin/hilla/generator/fixtures/SampleEndpoint/KindModel.ts",
                file.path());
        assertEquals("""
                import m from '@vaadin/hilla-models';
                import Kind from './Kind.js';

                const KindModel = m.enum(Kind, 'Kind');
                type KindModel = typeof KindModel;

                export default KindModel;
                """, file.content());
    }

    @Test
    public void should_WriteWhichSubtypeAValueOfAPolymorphicTypeCanBe() {
        // A subtype accepting the discriminator of the one below it is
        // narrowed to its own value, or a value of it would be a value of both
        var file = new UnionWriter().write(union(SampleEndpoint.Figure.class));

        assertEquals(
                "com/vaadin/hilla/generator/fixtures/SampleEndpoint/FigureUnion.ts",
                file.path());
        assertEquals(
                """
                        import type Blank from './Figure/Blank.js';
                        import type Ring from './Figure/Ring.js';
                        import type Round from './Figure/Round.js';

                        type FigureUnion = (Round & { '@type': 'round' }) | Ring | Blank;

                        export default FigureUnion;
                        """,
                file.content());
    }

    @Test
    public void should_WriteTheDiscriminatorOfASubtypeAsTheValuesItAccepts() {
        assertEquals("""
                import type Figure from '../Figure.js';

                interface Round extends Figure {
                  radius: number;
                  '@type': 'round' | 'ring';
                }

                export default Round;
                """,
                write(entity(SampleEndpoint.Figure.Round.class)).content());
    }

    @Test
    public void should_PinTheDiscriminatorOfTheModelOfASubtype() {
        // The empty value of a subtype has to say which subtype it is for the
        // server to read it back, so the model holds the ids the type accepts:
        // one of its own, or its own and the ones of the subtypes below it
        assertEquals(
                """
                        import m, { NumberModel } from '@vaadin/hilla-models';
                        import FigureModel from '../FigureModel.js';
                        import type Round from './Round.js';

                        const RoundModel = m
                          .extend(FigureModel)
                          .object<Round>('Round')
                          .property('radius', m.meta(NumberModel, { jvmType: 'double' }))
                          .property('@type', m.union(m.literal('round'), m.literal('ring')))
                          .build();
                        type RoundModel = typeof RoundModel;

                        export default RoundModel;
                        """,
                writeModel(entity(SampleEndpoint.Figure.Round.class))
                        .content());
        assertEquals("""
                import m from '@vaadin/hilla-models';
                import FigureModel from '../FigureModel.js';
                import type Blank from './Blank.js';

                const BlankModel = m
                  .extend(FigureModel)
                  .object<Blank>('Blank')
                  .property('@type', m.literal('blank'))
                  .build();
                type BlankModel = typeof BlankModel;

                export default BlankModel;
                """, writeModel(entity(SampleEndpoint.Figure.Blank.class))
                .content());
    }

    @Test
    public void should_WriteASubtypeWithNothingOfItsOwnAsItsIdAlone() {
        assertEquals("""
                import type Figure from '../Figure.js';

                interface Blank extends Figure {
                  '@type': 'blank';
                }

                export default Blank;
                """,
                write(entity(SampleEndpoint.Figure.Blank.class)).content());
    }

    @Test
    public void should_WriteADeclaredDiscriminatorAsTheIdRatherThanTwice() {
        // The property is declared by the type of the hierarchy which holds
        // it, so the subtype has it as the id it accepts and nothing else
        assertEquals("""
                import type Shaded from '../Shaded.js';

                interface Pale extends Shaded {
                  shade: 'pale';
                }

                export default Pale;
                """, write(entity(SampleEndpoint.Shaded.Pale.class)).content());
    }

    private static GeneratedFile write(EntityModel entity) {
        return new EntityWriter().write(entity);
    }

    private static GeneratedFile writeModel(EntityModel entity) {
        return new FormModelWriter(entities).write(entity);
    }

    /**
     * The type whose values the annotations of the validation API constrain,
     * which is generated on its own since nothing else of the fixtures is
     * constrained.
     */
    private static EntityModel validated() {
        return new FullStackGenerator(GeneratedTypeScriptTest.class,
                ValidatedEndpoint.class)
                .parseEntities().stream()
                .filter(entity -> entity.javaClass()
                        .equals(ValidatedEndpoint.Validated.class.getName()))
                .findFirst().orElseThrow();
    }

    private static UnionModel union(Class<?> javaClass) {
        return unions.stream()
                .filter(union -> union.javaClass().equals(javaClass.getName()))
                .findFirst().orElseThrow(() -> new AssertionError(
                        "Nothing was generated for " + javaClass));
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
