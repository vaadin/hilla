# The parser and the TypeScript generator

## Overview

The browser callable classes of an application go in, and the TypeScript the
browser calls them through comes out. The work is done in two halves.

The **parser** (`com.vaadin.hilla.parser`) walks the classes. It starts at the
endpoints, and goes down through their methods to the types those send and the
properties of those types. The parser itself only runs the walk: what the walk
finds, and what it means, is said by plugins, each of them handling one feature,
such as nullability or polymorphic types.

The **generator** (`com.vaadin.hilla.generator`) writes the files. Its model
(`generator.model`) holds what the writers need as plain records, and its
writers (`generator.typescript`) fill in the TypeScript from that model.

The two meet in `EndpointModelPlugin`. It is a parser plugin like the others,
and builds the model while the walk happens. When the walk is over,
`getGeneration()` hands the whole model to the writers as one `Generation`.

Every entry point (the Maven goal, the Gradle task, the Flow build task and the
development server) runs both halves through `TypeScriptProcessor.generate` in
`engine-core`.

## Design goals

The parser used to walk the classes naively. The current design comes from what
went wrong with that walk.

- **One walk instead of a loop for each level.** Every level of the old walk
  had its own nested, repetitive loop, with selective caches at each stage.
  Now one generic walk, `PluginExecutor`, replaces the loops and the caches. It
  does not know what it walks, so the walk is the same for an endpoint, a method
  or a type, and it has one cache: the scan result of each node.
- **Every node with its full context.** The old walk lost context, but some
  facts can only be understood with the whole path. For example, a Java type
  variable from reflection is just a name and some metadata, and nothing says
  where it belongs. Now every node is visited with the path it was reached by
  (`NodePath`). `EndpointExposedPlugin` resolves a type variable of an
  `@EndpointExposed` superclass to its argument by looking up the signature of
  the endpoint that extends it, further up the path.
- **Discover the nodes on the way.** The old walk iterated over a data structure
  that kept growing and changing while it was walked. Now the walk asks the
  plugins what is below each node as it reaches the node (`scan` returns the
  node's dependencies), so nothing iterates over a structure that is being
  changed.
- **One feature in one plugin.** In the old walk, every feature was spread
  across the whole code, which made contributing hard. Now plugins work under
  inversion of control, and each one contributes a single feature. The plugin
  that discovers or resolves a kind of node also handles it, in `enter` and
  `exit`. Plugins build on each other through the nodes and the facts on them,
  not by calling each other.

Kotlin support shows how this works. It is one plugin,
`KotlinNullabilityPlugin`. It replaces the nodes of Kotlin classes with `K*Node`s
that carry Kotlin reflection, and complements what the other plugins found,
without touching any of them.

## The walk

The walk starts at the endpoint classes and discovers a tree: the endpoints,
their methods, the parameters and return types of those, the types they refer
to, the properties of those types, the types of the properties, and so on.

At every node, the plugins are asked what the node contains. They are told when
the walk enters the node and when it leaves it, and they can say what a thing
really _is_: for example, a Spring Data `Page` is really a list.

A class the endpoints send is walked only once, even if many methods refer to
it. It is walked after the rest of what is already queued.

Facts are collected on the way down and settled on the way back up. For
example, a type gets its default nullability when the walk enters it, the
nullability annotations decide it when the walk leaves it, and the result is
then handed to the node above.

## Nodes

A node (`Node`) has two halves:

- Its **source** is what was found: a class, a method, a signature or a Jackson
  property, as a model of `com.vaadin.hilla.parser.models`.
- Its **target** is what the plugins say about it. It is mutable, and it is set
  during the walk.

Nodes are equal when their class and their source are equal (`AbstractNode`);
a `TypeSignatureNode` also compares its position among its siblings. The target
takes no part in it.

| Node                                            | Source                                                                 | Target                                                   |
| ----------------------------------------------- | ---------------------------------------------------------------------- | -------------------------------------------------------- |
| `RootNode`                                      | the browser callable classes                                           | none                                                     |
| `EndpointNode`                                  | an endpoint class                                                      | the endpoint name                                        |
| `EndpointSignatureNode`                         | the superclass or an interface of a class in an endpoint's hierarchy   | none                                                     |
| `EndpointExposedNode`, `EndpointNonExposedNode` | a class in an endpoint's hierarchy, with or without `@EndpointExposed` | none                                                     |
| `MethodNode`                                    | a public method of an endpoint or of an exposed class                  | none                                                     |
| `MethodParameterNode`                           | a method parameter                                                     | its name (`_param_N` when the bytecode has none)         |
| `TypeSignatureNode`                             | a type signature                                                       | `TypeFacts`                                              |
| `CompositeTypeSignatureNode`                    | the matching signatures of a property's field, getter and setter       | `TypeFacts`                                              |
| `EntityNode`                                    | a class the endpoints send                                             | `EntityFacts`                                            |
| `PropertyNode`                                  | a Jackson property of such a class                                     | its name, and `getValueType()` for the type of its value |
| `SubTypesPlugin.UnionNode`                      | a class annotated with `@JsonTypeInfo`                                 | the names of its subtypes                                |

`TypeSignatureNode` and `CompositeTypeSignatureNode` are both `TypedNode`s:
they are the nodes that stand for the type of a value.

## `NodePath`

A `NodePath` is a node together with the path of the node it was reached from,
up to the root.

- Plugins look up the context of a node through its path. For example,
  `NonnullPlugin` finds the package of the closest class for `@NullMarked`, and
  `TypeSignaturePlugin` checks whether a type is inside an entity.
- Two paths are equal when their nodes are equal all the way up to the root.
  This is what de-duplicates the walk: `PluginExecutor` never queues a path
  that it has queued before.

## `NodeDependencies`

`scan` returns the dependencies of a node as two lists:

- **Child nodes** are walked right below the node, in order, before the walk
  leaves the node.
- **Related nodes** are walked below the root, and are queued after everything
  that is already queued.

Entities are related nodes. Their path is always "root, then the entity",
whichever method reached them, so path equality walks each entity only once.
They are also walked outside the subtree that reached them, so an entity always
has the same context, and type variables in it are kept as type variables.

## The plugin lifecycle

A plugin (`Plugin`) can implement four hooks:

- `scan` says what is below a node, as `NodeDependencies`.
- `resolve` replaces a node before it is walked. This is how one node replaces
  another: `TransferTypesPlugin` maps a type to its stand-in, and
  `KotlinNullabilityPlugin` puts a `K*Node` in place of a node.
- `enter` runs on the way into a node, before anything below it is walked.
- `exit` runs on the way out of a node, after everything below it.

`PluginExecutor` keeps a deque of enter and exit tasks. To enter a node, it:

1. scans the node, or reuses the scan result of an equal node,
2. calls `enter`,
3. puts the node's exit task at the front of the deque,
4. resolves each child against the node's path, and puts the children's enter
   tasks in front of the exit task, in order,
5. resolves each related node against the root path, and queues it just
   before the root's exit task.

So the walk is depth-first, and a node is left only after everything below it
has been walked. A node instance can be reached on more than one path, because
the scan result is shared between equal nodes. This relies on the walk being
depth-first, and on the target being set again on enter: `TypeSignaturePlugin`
gives a typed node fresh `TypeFacts` every time it enters it.

## Reflection only

The walk runs on reflection: the endpoints are `Class` objects, and the models
of `com.vaadin.hilla.parser.models` are built with `ClassInfoModel.of(Class)`
and the like. `Parser` requires a class path, but the walk never reads
`classPathElements`.

## Ordering

The production order of the plugins comes from
`ParserProcessor.preparePlugins` and `ParserConfiguration.PluginsProcessor` (in
`engine-core`):

1. `EndpointModelPlugin`
2. `BackbonePlugin`
3. `MultipartFileCheckerPlugin`
4. `TransferTypesPlugin`
5. `KotlinNullabilityPlugin`, when it is on the class path
6. `NonnullPlugin`
7. `SubTypesPlugin`
8. `ModelPlugin`

The plugins run inside `PluginManager`, an `AbstractCompositePlugin`. It runs
`scan`, `resolve` and `enter` in this order, and `exit` in the reverse order.
Two things follow from this:

- `EndpointModelPlugin` comes first so that it exits last. It reads a node only
  after every other plugin has settled what it says about the node.
- `KotlinNullabilityPlugin` comes before `NonnullPlugin`, so it exits after it,
  and its nullability overrides the nullability that comes from annotations.

A plugin lists the plugins it builds on in `getRequiredPlugins`. When a composite
plugin is created, `verifyPluginsOrder` checks that each plugin comes after the
plugins it requires.

The test harness, `FullStackGenerator.defaultParserPlugins`, keeps its own copy
of this order. Change both together.

## The plugins

Each plugin owns one topic. It discovers or resolves its nodes, handles them
itself, and leaves facts on them for the plugins that exit after it.

### Backbone

`BackbonePlugin` builds the tree. It is a composite of small plugins, and the
others require it:

- `EndpointPlugin` makes an `EndpointNode` for each endpoint class, and names it
  from the annotation's `value` or from the simple name of the class.
- `EndpointExposedPlugin` walks the endpoint's hierarchy through
  `EndpointSignatureNode`s, and marks each class as exposed or not exposed. It
  also resolves type variables in exposed methods (see
  [Design goals](#design-goals)).
- `MethodPlugin` and `MethodParameterPlugin` add the public methods of
  endpoints and exposed classes, and their parameters.
- `TypeSignaturePlugin` adds the type nodes. These are a method's return type, a
  parameter's type, a property's type, an entity's non-JDK superclass, and
  the types a signature refers to. The types a signature refers to are the
  items of an array, an iterable or a map, the value of an optional, the
  argument of a type variable, and the bounds of a type parameter. On enter, it
  sets the type's starting `TypeFacts`. On exit, it hands them to the node above:
  to the property, or to the parent type when that type is written as its
  argument.
- `EntityPlugin` adds an `EntityNode`, as a related node, for every non-JDK
  class a type refers to, except classes sent as a value the browser already
  has, such as a date or a collection.
- `PropertyPlugin` adds the properties of an entity. It asks Jackson for the
  serialized properties, using the application's object mapper when
  `BackbonePluginConfiguration` names a factory for it. It keeps only the
  properties that the class itself declares; inherited ones come from the
  superclass entity.
- `JsonValuePlugin` resolves a type whose class has a `@JsonValue` method to
  the type that method returns.

### Nullability

`NonnullPlugin` decides on exit whether a value can be absent, and sets
`TypeFacts.setOptional`. It looks at the annotations on the type, on the method,
parameter or property the type belongs to, and on the package of the closest
class. Each known annotation has a score in `NonnullPluginConfig`, and the
highest score wins:

- 10: the package defaults, `@NullMarked` and `@NonNullApi`
- 20: the `@Nullable`s, plus JPA `@Id` and `@Version`, which make a value
  nullable
- 30: the `@Nonnull`s and `@NonNull`s

An `Optional` can always be absent. The plugin configuration can add or disable
annotations.

### Kotlin

`KotlinNullabilityPlugin` is loaded only when both the plugin class and
`kotlin-reflect` are on the class path (`ParserConfiguration.PluginsProcessor`).
It works on its own nodes:

- In `resolve`, it replaces the endpoint, method, parameter, entity, property
  and type nodes of Kotlin classes with `K*Node`s, which carry the `KClass`,
  `KFunction`, `KParameter`, `KProperty` or `KType`.
- In `scan`, it sorts the properties of a Kotlin entity in declaration order.
- In `exit`, it sets nullability from `isMarkedNullable`.

Its source is in `src/main/java/.../nonnull/kotlin` (the plugin) and
`src/main/kotlin/.../backbone/knodes` (the nodes).

### Model

`ModelPlugin` reads, on enter, what the annotations say about a value, for the
form models:

- Bean Validation constraints from `jakarta.validation.constraints` become
  `ValidationConstraint`s, under the `TypeFacts.CONSTRAINTS` note. Repeated
  constraints are unwrapped from their container.
- A property's JPA annotations from a fixed list (`@Id`, `@Version` and the
  four relationship annotations) become `Annotation`s, under
  `TypeFacts.ANNOTATIONS`.

The constraints on an array property belong to the array itself, not to its
items.

### Subtypes

`SubTypesPlugin` handles `@JsonTypeInfo` and `@JsonSubTypes`:

- In `scan`, it adds a `UnionNode` for a class annotated with `@JsonTypeInfo`,
  and an `EntityNode` for each class that `@JsonSubTypes` names, all as related
  nodes.
- On exit, it sets the union's subtypes, and the `EntityFacts.Discriminator` of
  each subtype.

A discriminator property is declared only when the type id is included as a
property (`As.PROPERTY` or `As.EXISTING_PROPERTY`) and is a name (`Id.NAME` or
`Id.SIMPLE_NAME`). For other strategies, the parser does not know the ids the
server sends, so it leaves the property out.

### Transfer types

`TransferTypesPlugin` resolves a typed node whose class has a stand-in to the
stand-in, and keeps the type arguments and annotations. For example, a Spring
Data `Page` becomes a `List`, `Pageable` becomes the stand-in of the same name,
and Reactor's `Flux` becomes the `Flux` stand-in. The `Flux` stand-in is a list,
which the generator recognizes by its class name. The stand-ins live in
`runtime-plugin-transfertypes`.

A stand-in that the generated TypeScript imports from a package, rather than
declaring it, says which package and which export with `@FromModule`. The
generator reads that annotation. This is how the signal types reach their
client counterparts.

### MultipartFileChecker

`MultipartFileCheckerPlugin` fails the walk when a `MultipartFile` is the type
of an entity property or a method's return type. A file can only be uploaded as
a method parameter.

## How facts reach the model

The plugins talk to `EndpointModelPlugin` only through the targets of the
nodes:

- `TypeFacts`, on typed nodes, says whether a value can be absent, and whether
  the type arguments of a type belong to the type itself. It also carries named
  _notes_ that a plugin writes and the model reads back, such as the
  constraints and annotations from `ModelPlugin`.
- `EntityFacts`, on entity nodes, carries the discriminator.
- `UnionNode`'s target lists the subtypes of a polymorphic type.

A new kind of fact goes in the same way: as a note, or as a field on one of
these classes. It does not go through a call from one plugin to another.

## Generator

### Design goals

- **Writers only fill in text.** The model answers the questions a Java type
  raises before any writer runs. For example, it already says whether a value
  can be absent, and whether a type is declared, provided by a package or
  written inline. A writer only turns the answer into TypeScript.
- **One path.** Every entry point writes through `TypeScriptProcessor.generate`,
  in the task that walked the classes, because that run has what the writers
  need. Don't add a second path that writes generated files.

### The model

`com.vaadin.hilla.generator.model` holds plain records: `Generation`,
`EndpointModel`, `MethodModel`, `ParameterModel`, `EntityModel`,
`PropertyModel`, `TypeModel` and `UnionModel`. The model is built from the Java
signatures the walk carries, so it keeps the Java type of each value, and can
tell a date, an instant and a string apart. It exists only while a generation
runs. Nothing writes it to a file.

`EndpointModelPlugin` builds it with a stack of frames:

- On enter, it pushes a frame for the node.
- On exit, it pops the frame, builds the node from what its children left in
  it, and adds the result to the parent's frame: a `TypeModel`, a
  `ParameterModel`, a `PropertyModel` or a `MethodModel`.
- Entities, unions and endpoints are collected at the top level.

A method goes to the closest `EndpointNode` frame, so the method of an exposed
superclass belongs to the endpoint it was reached from. Java allows overloads,
and TypeScript does not, so when two methods share a name, the last one wins.
Endpoints are keyed by name in the same way.

A `MethodModel.Kind` says how the client reaches a method:

- It _calls_ most methods.
- It _subscribes_ to a method whose return type is the stand-in for `Flux` or
  `EndpointSubscription`. The model then holds the type of the pushed values.
- It _builds a signal_ for a method that returns one of the signal stand-ins.
  There is one kind for each signal type.

Types that the TypeScript refers to by name instead of declaring them become
`TypeModel.Provided`. These are the file stand-in, which the browser has, and
the signal stand-ins, which name their module with `@FromModule`.

The unions are built in `getUnions()`, after the walk. A subtype that also
accepts the discriminators of its own subtypes is narrowed to its own value in
the union, and those values are known only once every subtype has been walked.

### The writers

`TypeScriptWriter.write` turns one `Generation` into a list of `GeneratedFile`s:

- for each entity, a declaration (`EntityWriter`) and a form model
  (`FormModelWriter`, `<Name>Model`),
- for each polymorphic type, a union (`UnionWriter`, `<Name>Union`),
- a file for each endpoint (`EndpointWriter`),
- the barrel `endpoints.ts` (`BarrelWriter`),
- and the client `connect-client.default.ts` (`ClientWriter`).

The writers are built from these parts:

- `ModulePaths` lays out the files. An entity's file mirrors its Java package,
  and nested classes become folders. Endpoint files sit at the top of the output
  folder. Imports between files are relative.
- `ImportRegistry` collects the imports of one file. Asking for the same import
  twice gives the same local name, and two different imports that want the same
  name get distinct ones. Names declared in the file are reserved, so that no
  import shadows them.
- `Template` fills named `{{holes}}` in a text block of TypeScript. A hole that
  is given no value throws.
- `Names` handles TypeScript identifiers: reserved words, free names and quoted
  property names.
- `TypeWriter` writes a `TypeModel` as a TypeScript type.
- `ModelWriter` writes a value's form model expression. `ModelConstraints` says
  which constraints the model library accepts on which kind of model, and
  `ModelCycles` decides which references between models are read lazily,
  because they are part of a cycle.

`OutputFolder` writes the files, and records them in `generated-file-list.txt`.
On the next run, it removes the files listed there that were not generated
again, and any folders left empty. It leaves everything else in the folder
alone, and does not touch a file whose content has not changed.

If the application has a `connect-client.ts` next to the output folder, the
endpoints import it instead, and `connect-client.default.ts` is not generated
(`TypeScriptProcessor.clientModule`).

### Package structure

```
com.vaadin.hilla.generator
├── model/             - What the writers need, as plain records,
│                        built while the parser walks the classes
└── typescript/        - The writers

com.vaadin.hilla.parser
├── core/              - The walk: nodes, paths and plugins
├── models/            - What a Java class, method or signature says about itself
└── plugins/
    ├── backbone/      - The endpoints, their methods and the types they send
    ├── model/         - What the annotations of a value say about it
    ├── nonnull/       - Nullability
    │   └── kotlin/    - Kotlin nullability (Kotlin source)
    ├── subtypes/      - Polymorphic types
    └── transfertypes/ - Mapped types and the MultipartFile check
```
