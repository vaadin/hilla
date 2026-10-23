# CRUD components

## Overview

`@vaadin/hilla-react-crud` builds a grid, a form, or both, from two inputs: the
model of an entity and a browser callable service. `AutoGrid` lists and
filters items, `AutoForm` edits one item, and `AutoCrud` puts the two side by
side. The application writes no per-type code: the columns and fields come
from reflecting on the model, and the data comes from a few service methods
that a Java service implements, or inherits from a repository service in
`com.vaadin.hilla.crud`.

This note follows the data: how the components read the model, how the grid
fetches pages and builds filters, how the three components compose, and what
the Java side has to provide. How a model describes its properties is
described in [`../models/ARCHITECTURE.md`](../models/ARCHITECTURE.md), and
the form hooks `AutoForm` uses in
[`../react-form/ARCHITECTURE.md`](../react-form/ARCHITECTURE.md).

## Design goals

- **No per-type code.** Columns, fields, renderers, header filters and the item
  id all come from the model's property types and annotations. The
  application only narrows or overrides them (`visibleColumns`,
  `columnOptions`, `fieldOptions`, `itemIdProperty`).
- **A small service contract.** The components need only `list`, optionally
  `count`, and `save`/`delete` for the form. An application can implement
  them by hand, or get all of them from `CrudRepositoryService` on a Spring
  Data JPA repository.
- **Both model systems.** Every component accepts a legacy model class from
  `@vaadin/hilla-lit-form` or a model from `@vaadin/hilla-models`. `ModelInfo`
  is the one place that tells them apart.

## Reading the model: `ModelInfo`

[`src/model-info.ts`](src/model-info.ts) wraps a model and answers questions
about its properties as `PropertyInfo`: the dotted path, a header text, a
`PropertyType`, the metadata and the property's model.

- A legacy model class is instantiated with `createDetachedModel`; a new model
  is used as is. Property names come from walking the prototype chain up to
  the respective `ObjectModel`, parents first, so inherited properties come
  before the subclass's own.
- Metadata is normalized to the `ModelMetadata` shape of
  `@vaadin/hilla-models`: legacy `javaType` and annotation `name` become
  `jvmType`. Everything after this point reads only that shape.
- `PropertyType` is looked up by JVM type first (`int` and `Long` become
  `integer`, `LocalDate` becomes `date`, and so on), then by model class
  (`StringModel`, `NumberModel`, `BooleanModel`, `EnumModel`, `ObjectModel`). A
  property with neither gets `undefined` and is not shown by default.
- The id property is `itemIdProperty` when given, otherwise the root property
  annotated with `jakarta.persistence.Id`, otherwise the one named `id`. The
  grid uses it as `itemIdPath`, and the form needs it to delete.

`getDefaultProperties` picks the columns and fields when the application does
not list them:

- A root property annotated with `jakarta.persistence.OneToOne` is replaced by
  its own properties (`address.street`, …), one level deep.
- Properties with no `PropertyType`, literal models (such as a type
  discriminator) and properties annotated with `jakarta.persistence.Id` or
  `jakarta.persistence.Version` are left out.
- Other relations stay as a single `object` property, shown as JSON.

## Fetching data

[`src/data-provider.ts`](src/data-provider.ts) turns the grid's data provider
callback into service calls.

- A `DataProvider` calls `list` with a `Pageable` built from the grid's page,
  page size and sort orders, and the current filter.
- `createDataProvider` picks the subclass by whether the service has a `count`
  method, which a generated service has when the Java service implements
  `CountService`:
  - `FixedSizeDataProvider` calls `count` with the filter for the grid size,
    and `count` with no filter for the total when asked. Both are cached until
    `reset` or `setFilter`.
  - `InfiniteDataProvider` makes up a size: one more than loaded while pages
    come back full, the exact size once a page comes back short. The size
    only grows while pages are full, so re-sorting while scrolled down does
    not shrink it.
- `useDataProvider`, `useGridDataProvider` and `useComboBoxDataProvider` are
  the public hooks for applications that use their own `Grid` or `ComboBox`.
  A new data provider function reference is what makes the component reload,
  so `refresh` bumps a counter that the memoized function depends on. The
  combo box always uses the infinite variant, and passes its filter string
  straight to the fetch callback.

## Filters

A filter is a `FilterUnion`: an `AndFilter` or `OrFilter` with `children`, or
a `PropertyStringFilter` that compares one property path to a string value
with a `Matcher` (`EQUALS`, `CONTAINS`, `LESS_THAN`, `GREATER_THAN`). The
`@type` property (`and`, `or`, `propertyString`) is how Jackson picks the Java
subclass of `Filter`.

`AutoGrid` keeps one root `AndFilter`. Each header filter component in
[`src/header-filter.tsx`](src/header-filter.tsx) builds a
`PropertyStringFilter` for its column and passes it up with the column's
`filterKey`; the grid replaces the child with the same `key`, drops it when its
value is empty, and passes the new root to the data provider. `key` is a
client-side addition to `FilterUnion` (see below) and is sent to the server
along with the filter.

`experimentalFilter` replaces the header filters' root filter entirely; the
two are not combined.

## The components

### `AutoGrid`

[`src/autogrid.tsx`](src/autogrid.tsx) renders a Vaadin `Grid`.

- One column per `PropertyInfo`. [`src/autogrid-columns.tsx`](src/autogrid-columns.tsx)
  maps the `PropertyType` to a renderer, alignment and header filter, and
  `columnOptions` are spread over that.
- With header filters on, each column sits in a `GridColumnGroup`: the group
  header holds the `HeaderSorter`, the column header holds the filter. A
  `ColumnContext` carries the property, the sort state and `setColumnFilter`
  to those header renderers. Custom columns get the same treatment through
  `CustomColumnContext`.
- The first sortable column is sorted ascending at the start. The data
  provider is created in a `setTimeout` after mount, so that the sorters are
  rendered and the first request already carries that sort.
- A filter change calls `setFilter` on the existing data provider and
  `clearCache` on the grid. `refresh` on the `AutoGridRef` does the same with
  `reset`, which also forgets cached counts.

### `AutoForm`

[`src/autoform.tsx`](src/autoform.tsx) is a form over `useForm` from
`@vaadin/hilla-react-form`, whose submit calls the service's `save`.

- The `item` prop is read into the form; the `emptyItem` symbol, the default,
  clears it for a new item. After a new item is saved, the form clears itself,
  since the prop would not change and nothing else would clear it.
- [`src/autoform-field.tsx`](src/autoform-field.tsx) renders one field per
  `PropertyInfo`, chosen by `PropertyType`. Each field uses `form.field` for
  the binding and `useFormPart` to add `fieldOptions` validators to the
  property's own.
- A `ValidationError` shows its messages that are not tied to a field; an
  `EndpointError` goes to `onSubmitError`, or is shown as is. `save` returning
  `undefined` counts as a failed update.
- Delete reads the id from the item through `ModelInfo.idProperty`, so the
  delete button only shows when an id property is found.

### `AutoCrud`

[`src/autocrud.tsx`](src/autocrud.tsx) holds the edited item as state and
passes it to both: the grid's active item becomes the form's `item`, and a
save or delete in the form refreshes the grid through its ref. Below 600 px in
width or height the form moves into a dialog. The service must be a full
`CrudService`.

Each component is exported through a `*-feature.ts` wrapper that registers its
use for Vaadin's usage statistics.

## The checked-in generated types

The components need the TypeScript of `Filter`, `Pageable` and `Sort` at build
time, before any application generates them. Copies live under
[`src/types/`](src/types), in the paths the generator would write them to.

- They are not regenerated by any build step, and they drift. The `*Model.ts`
  files are in the legacy `@vaadin/hilla-lit-form` format, and the components
  don't use them; only the interfaces and enums are imported.
- `FilterUnion.ts` was edited by hand to add the optional `key`.
- When a Java filter, `Pageable` or `Sort` class changes, update these copies
  to match.

## The Java side

The services live in
[`com.vaadin.hilla.crud`](../../java/endpoint/src/main/java/com/vaadin/hilla/crud)
in the `endpoint` module, and match the TypeScript interfaces in
[`src/crud.ts`](src/crud.ts).

- The interfaces are `ListService`, `CountService`, `GetService`,
  `FormService`, and `CrudService`, which is `ListService` plus `FormService`.
  An application service implements the ones its components need.
- `ListRepositoryService` implements list, get and count on a repository that
  is both a `CrudRepository` and a `JpaSpecificationExecutor`.
  `CrudRepositoryService` adds save and delete. Both are `@EndpointExposed`,
  so a `@BrowserCallable` subclass exposes their methods.
  - The entity class and, unless passed to the constructor, the repository
    are resolved from the subclass's type arguments; the repository is
    looked up as a bean after construction.
  - The page size is capped by `spring.data.web.pageable.max-page-size`, or
    2000 without Spring Data Web.
- The Java methods take Spring's `Pageable`, which the transfer types map to
  and from `com.vaadin.hilla.mappedtypes.Pageable` on the wire.
- `JpaFilterConverter.toSpec` turns a `Filter` into a JPA `Specification`:
  `AndFilter` and `OrFilter` combine their children, and a
  `PropertyStringFilter` becomes a `PropertyStringFilterSpecification`. A
  custom JPA service can call it directly.
- `PropertyStringFilterSpecification` parses the string value by the type of
  the entity attribute (strings, numbers, booleans, `UUID`, `LocalDate`,
  `LocalTime`, `LocalDateTime`, enums) and throws `IllegalArgumentException`
  for a matcher that makes no sense for the type. Strings compare without
  case; `EQUALS` on a `LocalDateTime` matches the whole day.
- `FilterTransformer` renames properties in a `Filter` and a `Pageable`, for a
  service whose items are DTOs whose property names differ from the
  entity's.

## Testing

The tests in [`test/`](test) run the components against fake services and
models from `@vaadin/hilla-models`; [`test/old/`](test/old) runs the same
tests with legacy models. A change to `ModelInfo` or a component should pass
both. The Java services are tested in the `endpoint` module's
`com.vaadin.hilla.crud` tests, and end to end in
`packages/java/tests/spring/react-grid-test`.
