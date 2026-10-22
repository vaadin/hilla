# Full-stack signals

## Overview

A full-stack signal is a Preact signal in the browser whose state lives in a
shared signal on the server (`SharedValueSignal`, `SharedNumberSignal`,
`SharedListSignal`, `SharedMapSignal` from Flow's `com.vaadin.flow.signals`).
An endpoint method returns the server signal; the generated function for that
method returns a client signal instead of calling the method:

```ts
const items = ListSignalService.items();
items.insertLast({ text: 'Milk', completed: false });
```

Every client that renders the signal sees every change made by any client, or
by the server. A change made in the browser is visible right away and is
reverted if the server rejects it.

The state of a server signal is a tree of nodes, and changes to it are
commands: `set`, `inc`, `insert`, `put`, `remove`, and so on. The client keeps
a copy of that tree and speaks the same commands. This note follows a signal
from the generated code to the server and back: the generator, the client
tree, the connection, and the server side.

## Design goals

- **One model on both sides.** The client applies commands to a tree the same
  way the server does (`MutableTreeRevision`), and keeps the same split
  between a confirmed state and the commands that wait for confirmation
  (`AsynchronousSignalTree`). Code that matches the server's behaviour is
  preferred over code that approximates it.
- **Latency compensation.** A change is applied locally when it is made. The
  server decides whether it stands; a rejected change disappears without the
  application doing anything.
- **Just a signal.** A full-stack signal is a `Signal` subclass. Rendering it
  in JSX, reading it in `computed`, `effect` or `useSignalEffect` works as for
  any other signal, and the children of a list or a map are signals of their
  own.
- **Connected only while used.** The subscription to the server is opened when
  the signal gets its first watcher and closed when it loses the last one.
- **The endpoint method decides who gets the signal.** Subscribing invokes the
  endpoint method with the access check of a regular call, and every later
  command is checked again.

## Generated code

The parser maps the Flow signal types to the stand-in types in
`runtime-plugin-transfertypes` (`TransferTypesPlugin`), which say which class
of `@vaadin/hilla-react-signals` to use (`@FromModule`). The generator reads a
method returning one of them as a signal of its own kind
(`EndpointModelPlugin`, `MethodModel.Kind`), and `EndpointWriter` writes it
differently from a call:

- The function is not `async`, has no `init` parameter, and returns
  `new ValueSignal(...)` (or the `NumberSignal`, `ListSignal`, `MapSignal`
  class) with the client, the endpoint and method names, and the method's
  parameters as `params`.
- A `ValueSignal` gets an `options?: SignalMethodOptions<T>` parameter for its
  default value. Without one, the default is `undefined` for a nullable type,
  and the empty value of the type's model otherwise. A `NumberSignal` defaults
  to `0`. Lists and maps have no default.

Calling the generated function creates a new client signal. It costs nothing
until it is watched, but two calls give two signals with two connections, so
applications create the signal once, usually at module level.

## The client tree

### Nodes

A `NodeTree` ([src/NodeTree.ts](src/NodeTree.ts)) is an immutable map from
node id to `NodeData`: a value, ordered list children, keyed map children and
the parent. The root has the id `''`. `snapshotToTree` turns the server's
nodes into a tree; alias nodes are skipped, since only commands the client
never sends create them.

`applyCommand` returns a new tree, or `null` when the command doesn't apply,
and mirrors `MutableTreeRevision`:

- A node created by `insert` or `put` gets the id of the command that created
  it. The client creates the id, so an optimistically created node has the
  id the server will give it, and later commands can target it before the
  server has confirmed it.
- `findInsertIndex` resolves a `ListPosition` (after and/or before a sibling,
  `''` meaning the edge of the list) the way the server does.
- `put` to an existing key sets the value of the existing child, so the
  child's signal stays the same.
- `inc` treats a missing value as `0`. `value` and `pos` are conditions: they
  change nothing and fail when the node doesn't match, with values compared
  as JSON. A `tx` applies all of its commands or none.
- `clear` and `remove` drop the subtree; `at` moves a child and keeps its
  subtree.

The command types and their factories are in [src/commands.ts](src/commands.ts);
the JSON is the same as Flow's `SignalCommand` subtypes, with `@type` as the
discriminator.

### Confirmed and unconfirmed state

`SignalTree` ([src/SignalTree.ts](src/SignalTree.ts)) holds the state of one
server signal:

- `#confirmed`, the last tree the server has confirmed. Until the server sends
  one, it is a root holding the default value.
- `#unconfirmed`, the commands submitted but not yet confirmed or rejected.
- `nodes`, a `computed` that replays the unconfirmed commands on the confirmed
  tree. That is the visible state every signal reads. A command that no longer
  applies is skipped, and the rest are still replayed.
- `pending` (some command waits) and `error` (the last failure).

`submit(command)` appends the command to the queue, which makes it visible
through `nodes`, sends it, and returns an `Operation` whose `result` resolves
on confirmation and rejects on rejection. The promise is always given a
handler, so an application that ignores the result gets no unhandled
rejection.

The server's answer comes back as the same command, with `accepted` and, when
rejected, a `reason` (`handleCommand`):

- Accepted: the command is applied to `#confirmed` and removed from the
  queue. Commands made by other clients and by the server arrive the same way
  and are simply applied.
- Rejected: the command is removed from the queue, which is the whole revert,
  and its operation is rejected with the reason.
- A `snapshot` replaces `#confirmed` and leaves the queue alone; the commands
  in it are replayed on the new state, as on the server.

When the `update` call itself fails, the command is dropped as if it were
rejected, and the failure is set as `error`.

### Signals as views of the tree

`FullStackSignal` ([src/FullStackSignal.ts](src/FullStackSignal.ts)) is a
`Signal` whose value is derived from `tree.nodes` for one node `id`. Each
subclass says which part of the tree it shows (`deriveValue`) and which
commands its methods submit:

- `ValueSignal`: the node's value, or the default while there is none. Setting
  `value` or calling `set` sends `set`, which overwrites whatever the server
  has (last write wins).
- `NumberSignal`: `incrementBy` sends `inc`, which is applied to the server's
  value, so concurrent increments all count. A zero delta sends nothing.
- `ListSignal`: the list children as `ValueSignal`s. `insertFirst`,
  `insertLast`, `insertAt`, `remove` and `clear`. Setting `value` throws.
- `MapSignal`: the map children by key as `ValueSignal`s. `put`, `remove` and
  `clear`. Setting `value` throws.

The signal of the endpoint method is the root. Its children are created on
the same `SignalTree` with their node id, so they share its state and its
connection: a child is a full signal that can be set, watched and passed
around on its own. `ChildSignals` ([src/ChildSignals.ts](src/ChildSignals.ts))
caches them by node id, so a child keeps its instance across updates for as
long as its node exists, and is dropped when the node goes. `insertAt` and
`put` return the signal of the new entry in the operation, usable right away
because its id is known before the server answers.

## Connecting to the server

`Connection` ([src/Connection.ts](src/Connection.ts)) talks to one endpoint,
`SignalsHandler`, with two methods:

- `subscribe(providerEndpoint, providerMethod, clientSignalId, params)`, a
  `Flux` of commands, carried by the push connection of
  `@vaadin/hilla-frontend` (see
  [`../frontend/ARCHITECTURE.md`](../frontend/ARCHITECTURE.md#subscribing)).
- `update(clientSignalId, command)`, a regular call, sent with `mute: true` so
  it doesn't show in Flow's loading indicator.

The `clientSignalId` is random per `SignalTree`. It names the client side of
the signal on the server, so every later message is matched to the same
server signal without repeating the endpoint and parameters.

`SignalTree.nodes` subscribes when it gets its first watcher and cancels the
subscription when it loses the last one (the `watched` and `unwatched` options
of `computed`). When the push connection is lost, the subscription is made
again (`onSubscriptionLost` returns `resubscribe`), and the server answers
with a fresh snapshot.

A signal can also be changed while nobody watches it. Then `send` subscribes
for the duration of the call and cancels afterwards. Since no confirmation
can arrive, the command is confirmed when the call succeeds. If a watcher
subscribed in the meantime, the snapshot it got already contains the command,
so the command is only removed from the queue, not applied again.

## The server side

The server code is in
[com/vaadin/hilla/signals](../../java/endpoint/src/main/java/com/vaadin/hilla/signals).

- `SignalsConfiguration` creates `SecureSignalsRegistry` only when the
  `fullstackSignals` feature flag is on. `SignalsHandler` is always an
  endpoint (`@AnonymousAllowed`), and throws an explanation of the flag when
  the registry is missing.
- `SecureSignalsRegistry.register`, on the first `subscribe` of a client
  signal id, checks access to the provider method and invokes it through
  `EndpointInvoker`, with the parameters, like a call. `get`, used by every
  later `subscribe` and `update`, checks access again.
- `SignalsRegistry` maps each client signal id to one `InternalSignal` per
  server signal tree. Clients that subscribe to the same server signal share
  it. Unsubscribing removes the client's mapping.
- `InternalSignal` wraps the Flow `SignalTree` of the signal.

On subscribe, `InternalSignal` adds a sink for the client, starts listening to
the tree's processed commands when it is the first subscriber, and sends a
`snapshot` of the confirmed nodes. When the last subscriber goes, it stops
listening.

On update, it reads the command, remembers the client's JSON and client
signal id by command id, and commits the command to the tree, under the
tree's lock. When the tree has processed a command:

- The JSON to send is the client's JSON, or the processed command serialized
  when the command came from the server (for example `signal.set(...)` in
  Java). It gets `accepted`, and the `reason` when rejected.
- An accepted command goes to every subscriber, including the one that sent
  it, which is its confirmation. A subscriber whose sink fails is removed.
- A rejected command goes only to the client that sent it.

## Working on it

- Unit tests: [`test/NodeTree.spec.ts`](test/NodeTree.spec.ts) for the tree,
  and one spec per signal class for submitting, confirming and rejecting.
  [`test/utils.ts`](test/utils.ts) stubs the subscription, and its
  `simulateReceivedChange` plays the server's part.
- Server: `InternalSignalTest` and the other tests in
  `packages/java/endpoint/src/test/java/com/vaadin/hilla/signals`.
- Generator: the `transfertypes/signals` case of `EndpointGenerationTest`
  in `packages/java/typescript-generator`.
- Integration tests with several browsers on one signal:
  `packages/java/tests/spring/react-signals`.
- When the tree logic changes, compare it with `MutableTreeRevision` in Flow.
  A difference shows up as an optimistic change that jumps or vanishes when the
  server confirms it.
