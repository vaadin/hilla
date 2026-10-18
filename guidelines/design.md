# Design

Guidance for the shape of new public API in Hilla. Consult this before starting
any non-trivial new API. Hilla has three kinds of public API, and a feature
often touches all three:

- the **generated TypeScript** an application imports from
  `frontend/generated/`,
- the **Java API** an application uses to write endpoints and configure Hilla,
- the **TypeScript packages** an application imports from `@vaadin/hilla-*`.

## Before you start

- **Find the precedent.** Hilla already has patterns for endpoint annotations,
  access control, mapped types (`Pageable`, `Flux`, signals), parser plugins and
  their configuration, React hooks and form binders. Look at
  `@BrowserCallable`, `EndpointSubscription`, the plugins under
  `com.vaadin.hilla.parser.plugins`, `useForm` and `AutoGrid` — match the
  existing shape rather than inventing a new one.
- **Understand the blast radius.** See [Repository](repository.md) — changes to
  the parser, the generator model or the wire format reach every application.
- **The packages are a contract.** A change that spans the Java runtime, the
  generated TypeScript and a TypeScript package updates all of them in the same
  PR.

## The generated TypeScript

What the generator writes is public API as much as any class: applications
import it, and an upgrade that changes it breaks their build or, worse, their
behavior.

- **Decide the TypeScript from the Java an application writes.** A Java type
  maps to one TypeScript shape, and that mapping is predictable from the
  Java declaration, its nullability and its Jackson annotations — not from
  where the type happens to be used.
- **Follow what Jackson does.** The generated type of a value has to describe
  what the endpoint actually sends and accepts. When the generated type and
  the serialized value disagree, the generator is wrong.
- **Read the snapshot diff as the specification.** Every change to the
  generated output shows up in the snapshots; review that diff as carefully as
  the code that caused it, and call out changes to existing output in the PR.
- **Every name becomes a TypeScript identifier.** Endpoint, method, parameter
  and type names have to be valid there too; see `EndpointNameChecker`.

## Java API

### Accepting a type the application writes

Browser callable classes, the types they send, and plugin configuration are
written by the application and interpreted by Hilla. Validate such a type where
it is accepted — at registration or during the walk — and report what is wrong
with it in terms of the application's code.

The judgment call that leaves open is what happens to a member the mechanism
does not interpret. Do not let that case through unspoken. When some members
of such a type are interpreted and others are ordinary Java, the type carries
two kinds of member with different rules and the user has to know which is
which. That can be the right design, but then the line between them is a rule
stated in one sentence in the Javadoc and enforced for everything on the
interpreted side. What must never happen is a member that quietly does nothing
— or is quietly left out of the generated TypeScript — because it fell between
the two.

### Options records

For tunable knobs, prefer an immutable Java record over a long parameter list:

- Compact constructor with validation (reject negative durations, etc.).
- A builder when there are enough optional components to make the canonical
  constructor hard to read at the call site.

### Interfaces vs abstract classes

When introducing a new type that callers will receive or pass around, pick
*one* of: a single concrete / abstract class named `Xyz`, or both an interface
`Xyz` and an abstract class `AbstractXyz`. Don't introduce one half
speculatively.

- **Default to a single abstract (or concrete) class named `Xyz`.** Skip the
  interface unless you can demonstrate, today, a useful implementation that
  does *not* extend the abstract class. Introducing the interface later does
  not retroactively help: every existing method signature that accepts
  `AbstractXyz` will still reject interface-only implementations.
- **Only add an interface alongside an abstract class if you can ship a worked
  example of using the interface without the abstract class.**
- **Name it `Xyz`, not `AbstractXyz`, unless an interface exists or is
  explicitly planned.** The class name appears in every method signature that
  accepts an instance.
- **Prefer an interface with default methods over an abstract class when there
  is no state.** Abstract classes earn their keep by holding fields.
- **Don't make a sealed interface that permits a single abstract class.** It
  serves no caller: either keep just the abstract class, or open the interface
  up to genuinely independent implementations.

### Result types: sealed interfaces + pattern matching

For values that are "one of N things", use a sealed hierarchy with record
subtypes:

```java
public sealed interface Foo permits FooA, FooB, FooC {}
```

Exhaustive `switch` expressions over the sealed set are guaranteed complete at
compile time; design for that, don't add `default:` arms. A sealed hierarchy
sent through an endpoint is also a polymorphic type for the `subtypes` plugin —
check what it generates.

### Nullability discipline

- Annotate with JSpecify (`org.jspecify.annotations.Nullable` / `NonNull`).
  Only mark `@Nullable` what genuinely may be null. In endpoint signatures
  and the types they send, the annotation decides the generated TypeScript
  type — see [Architecture](architecture.md#nullability).
- **Collections: return empty, never `null`.** A method returning a `List`,
  `Set`, `Map`, `Stream` or array returns an empty one when there are no
  values.
- **Optional properties: return `@Nullable T`, not `Optional<T>`**, in new
  API, matching a setter that accepts `null` to clear the value. Don't
  change existing `Optional`-returning methods.
- **Don't encode "not set" as a fake value.** Return `null`, not `""`, `-1`
  or `0`, when nothing is set — a stand-in value can not be told apart from a
  value the application actually set.
- **Named states instead of `null`.** When a value has a real "no data yet"
  state, model it as an enum constant (`UNKNOWN`) or a record in a sealed
  hierarchy (`Pending`) so callers handle it in an exhaustive `switch`.

### Extending an existing entry point

Overloading an existing method, rather than adding a name for a second way to do
the same thing, is a discoverability decision: the user who knows one form
finds the other among the overloads of what they already call, while a separate
name has to be found on its own and reads as a different feature.

The cost lands in the documentation. Overloads that do the same thing
differently have to open with the same sentence about what the method is for
and then say which is which. That is cheaper than a name nobody finds.

## TypeScript package API

- **Export deliberately.** What a package exports from its entry points
  (`exports` in `package.json`) is public API; keep helpers internal unless an
  application needs them.
- **Keep React and Lit counterparts aligned.** When a concept exists for both
  (e.g. the form binders), give it the same name and the same semantics in
  both, unless the framework forces a difference.
- **React APIs follow React conventions.** Hooks are named `useXxx` and follow
  the rules of hooks; components take props rather than imperative setters.
- **Types are the documentation the editor shows.** Prefer precise types —
  discriminated unions, generics tied to the generated models — over `any` or
  loose records.
