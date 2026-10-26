# File router architecture

## Overview

The file router turns the files under the views folder (`frontend/views/` by
default) into React Router routes. A route's path comes from where its file
is, and its metadata (title, access, menu entry) from a `config` object the
file exports next to its component.

The route data travels through three stops:

1. **The Vite plugin**, at build time and in the dev server, reads the views
   folder and writes two files: `file-routes.ts`, the route tree the browser
   imports, and `file-routes.json`, the same tree with the metadata, for the
   server.
2. **The runtime**, in the browser, turns `file-routes.ts` into a React Router
   configuration with `RouterConfigurationBuilder`, and builds the menu from
   the views the server sends.
3. **The server** reads `file-routes.json` to answer which client views exist
   and who may open them: it builds the menu data it injects into
   `index.html`, and it does the access check of a request for a client view.

Flow owns parts of each stop: it adds the plugin to the Vite configuration,
writes `layouts.json` and the default `routes.tsx`, and reads
`file-routes.json` in `MenuRegistry`.

## Design goals

- **The file system is the configuration.** Adding a view is adding a file.
  The metadata is read statically from the `config` export, so neither the
  build nor the server has to run the view.
- **Unused views cost nothing at run time.** Routes are lazy by default: the
  browser loads a view's module only when the view is opened, and its
  metadata is inlined in `file-routes.ts` so that the router and the menu do
  not need the module.
- **One source of route data.** The same files drive the client router, the
  server-side menu and the server-side access check, so they cannot disagree
  about which views exist and who may see them.
- **Fast feedback in development.** Generated files are rewritten only when
  their content changes, and a change to the views updates the router and the
  menu through HMR, without a page reload.

## The Vite plugin

`vitePluginFileSystemRouter` ([`src/vite-plugin.ts`](src/vite-plugin.ts)) is
the entry point. Flow's `TaskUpdateVite` adds it to `vite.generated.ts` when
React is enabled and Hilla is used, as
`vitePluginFileSystemRouter({isDevMode: devMode})`. Each run of
`generateRuntimeFiles` goes through the steps below.

### Collecting the routes

`collectRoutesFromFS` walks the views folder into a tree of `RouteMeta`. The
naming rules:

- A file name is a path segment. `{param}` is a required parameter, `{{param}}`
  an optional one and `{...wildcard}` matches the rest of the path.
- `@index` is the directory's own page, and `@layout` wraps the other routes of
  its directory.
- Files and folders starting with `_` are private and skipped.
- Only the plugin's `extensions` (`.tsx` and `.jsx`) are routes. A `.ts` or
  `.js` file gets a warning and is skipped.
- An empty file is skipped; a file without `export default` is logged as an
  error and skipped.

It throws for a file and a folder with the same name, an `@index` next to an
optional parameter, and any other `@` name.

### Applying Flow layouts

`applyLayouts` reads `layouts.json` from the generated folder, which Flow's
`TaskGenerateReactFiles` writes with the paths of the `@Layout` classes (and
rewrites on hotswap). Each route under one of those paths gets `flowLayout:
true`, so that the runtime renders it inside the server-side layout. Without
the file, the routes are left as they are. A `flowLayout` set in the view's
config wins over this flag, in the route module, in `file-routes.json` and in
the runtime: `flowLayout: false` opts a view out of a server-side layout that
covers its path (#4430).

### Reading the metadata

`createViewConfigJson` turns the tree into `ServerViewConfig` objects, the
content of `file-routes.json`. For each file it parses the source with the
TypeScript compiler, finds the `config` variable whose initializer is an object
literal, and evaluates that literal's text with `node:vm`. So `config` must be
a self-contained literal: it can not refer to imports or to other variables.
The one exception is react-i18n's `` key`...` `` tag, which the script defines as
returning its string. The title falls back to the name of the default export,
split at capitals and without `View`. The route's parameters are added as
`params`.

### Writing the route module

`createRoutesFromMeta` writes `file-routes.ts`: a tree of `createRoute` calls
with the component and the config of each route. It is the only user of
`@vaadin/hilla-generator-utils`.

- A lazy route imports its module with `React.lazy`, and its config is inlined
  from the JSON. `/` and `/login` are eager unless `config.lazy` says
  otherwise, and so is any route with `lazy: false`; an eager route imports
  the module and reads its `config` export.
- Two routes with the same path become a `console.error` in the generated
  module, not a build error.

### Writing only on change

`generateRuntimeFile` compares the new content with the file on disk and
writes only when it differs, which spares the server and Vite a reload. When
the JSON changed, `file-routes.ts` is written even if its own text is the
same, so that Vite's HMR picks up the new metadata of the views it imports.

### Where the files go

`file-routes.ts` is always written to the generated folder
(`frontend/generated/`). `file-routes.json` goes there too in development
(`isDevMode`), and next to the bundle in a production build.

In production `file-routes.json` is resolved as
`new URL('file-routes.json', pathToFileURL(outDir))`. Flow sets `outDir` to
`…/classes/META-INF/VAADIN/webapp`, with no trailing slash, so URL resolution
drops `webapp` and the file lands in `META-INF/VAADIN/`, the classpath location
Flow's `MenuRegistry.FILE_ROUTES_JSON_PROD_PATH` expects. The same rule puts it
in `dev-bundle/` for a dev bundle build, where `DevBundleUtils.findBundleFile`
looks for it.

**This is fragile.** It depends on `outDir` having no trailing slash and on
Flow expecting the JSON one level above it. If either changes, the file ends
up inside `webapp/`, where it is served as a public static file, and the server
silently finds no client views: they get no menu entries, and the server-side
access check treats them as unknown paths.

In development the files are generated in `configResolved`, before Vite's
`configureServer` starts the TypeScript checker, so `file-routes.ts` exists for
the first type check. A production build generates them in `buildStart`.

## Development updates

`hotUpdate` reacts to changes in the views folder and to `layouts.json`:

- It regenerates the files. When `file-routes.ts` changed, the plugin
  invalidates and refetches it in the module graph and adds it to the update,
  so that the router and the changed view update together. A file nothing
  imports yet (a new or removed view) updates only `file-routes.ts`, instead
  of Vite's default full reload.
- A change to `file-routes.json` sends the custom `fs-route-update` event. The
  `createMenuItems` module listens for it, fetches `?v-r=routeinfo` from the
  server and updates `viewsSignal`, so the menu follows.
- A view exports `config` next to its component, which React Fast Refresh does
  not allow. In development `transform` registers each route file in
  `window.__HILLA_FILE_ROUTER_VIEWS__` and installs a
  `__getReactRefreshIgnoredExports` hook (the
  `virtual:hilla-file-router-refresh-ignore` module) that excludes `config` for
  these files. Changes to `config` reach the browser through `file-routes.ts`
  instead.

In production `transform` appends an `Object.defineProperty(…, 'name', …)` for
the default export of each view, so that the component name, used as the
fallback title at run time, survives minification.

## The runtime

### Building the router

The application builds its router with `RouterConfigurationBuilder`
([`src/runtime/configuration-builder`](src/runtime/configuration-builder)).
Flow writes a default `routes.tsx` to the generated folder, which an
application replaces by having its own `frontend/routes.tsx`. It amounts to:

```ts
new RouterConfigurationBuilder().withFileRoutes(fileRoutes).withFallback(Flow).protect().build();
```

Each method records a modifier, and `build` applies them in the order they
were called, each to the tree the previous ones produced. The order matters:
`protect` and `withFallback` only change the routes that are already in the
tree.

- `withFileRoutes` and `withReactRoutes` merge routes into the tree with
  `mergeRouteTrees`. Routes merge by path and by whether they have children
  (`createRouteKey`), so an explicit React route can override or extend a file
  route. Adding two routes with the same key in one call throws.
  `fileRouteTransformer` turns a file route into a `RouteObject` whose
  `handle` is the view's config.
- `withFallback` (once) adds the fallback component — Flow's server-side
  router — as a `*` route and an index route to each branch, so that a path
  the client does not know goes to the server. It also calls `withLayout`.
- `withLayout` (once) moves the routes with `flowLayout` under a pathless route
  that renders the given component, which renders the server-side layout
  around them.
- `protect` passes each route to `protectRoute` from
  `@vaadin/hilla-react-auth`, which wraps the ones whose handle has
  `loginRequired` or `rolesAllowed` in a login and role check.
- `build` always appends `withLayoutSkipping`, which moves the routes with
  `skipLayouts` to a copy of their branch with the layout elements removed.
  It then creates the browser router, with the document base URI as its
  `basename`.

The client checks access for the user experience only; the server enforces it
(see below).

### Fallback to Flow and Flow layouts

The fallback component is Flow's `Flow` component from the generated
`Flow.tsx`. Hilla only uses it as a React component: it never reads Flow's
`serverSideRoutes`, and how `Flow` renders a server view is Flow's business
(Flow's `flow-client/ARCHITECTURE.md`). What Hilla decides is where `Flow` goes
in the tree.

`withFallback` records `withLayout` first, then the fallback, so the layout
split happens before fallbacks are added:

1. `mergeLayout` sorts the routes by `flowLayout`. A route with the flag set to
   `true` goes under the server layout with its children, except the children
   that set it to `false`; a route with `false` stays a client route, and so do
   routes without the flag whose ancestors don't have it. A parent whose
   children are split ends up in both groups, each copy with its own children.
   The server routes are wrapped in one pathless route that renders the layout
   component and carries the `IGNORE_FALLBACK` flag.
2. `createFallbackTransformer` adds the fallback to every branch that has
   children, except the ones flagged `IGNORE_FALLBACK`, which are the server
   layout routes; a path under a Flow layout that no client view matches
   reaches the top-level fallback instead. A branch with a wildcard child gets only the index
   fallback, a branch with an index or optional child only the wildcard
   fallback, and any other branch both. Leaf routes are left alone.
3. The fallback routes are also merged in at the top level, so the
   application falls back to Flow even with no client routes at all, or with
   only a client layout and nothing inside it (#2989, #3280, #3883).

### View config and menu

`useViewConfig` returns the handle, that is the config, of the deepest
matching route.

`createMenuItems` does not read the routes. It reads `window.Vaadin.views`,
which the server injects (through `viewsSignal`, which the development updates
above refresh), and returns the views not excluded from the menu and without a
`:` parameter, ordered by `menu.order` and then by path. This list already
contains the server-side views the user may open.

## The server side

The server side lives in
[`com.vaadin.hilla.route`](../../java/endpoint/src/main/java/com/vaadin/hilla/route)
and [`RouteUnifyingServiceInitListener`](../../java/endpoint/src/main/java/com/vaadin/hilla/startup/RouteUnifyingServiceInitListener.java).
Flow's `MenuRegistry` reads `file-routes.json` for both: from the generated
folder (or the dev bundle) in development, and from the classpath, cached, in
production. It flattens the tree into a map from the full path to an
`AvailableViewInfo`.

### Injecting the views

`RouteUnifyingServiceInitListener` runs when React is enabled. It registers
`RouteUnifyingIndexHtmlRequestListener`, which adds a script setting
`window.Vaadin.views` to every `index.html` response. In development it also
registers the `?v-r=routeinfo` handler that returns the same JSON.

`ServerAndClientViewsProvider` builds that JSON for the current request:

- The client views from `MenuRegistry`, filtered by the user's access, without
  layouts and without views that have a required parameter.
- With `vaadin.expose-server-routes-to-client` (on by default), the server
  views that Flow's menu would show: only when Flow's `MenuAccessControl` says
  to always populate the client menu, or when the file routes have a main
  layout at the root. They are filtered by the `NavigationAccessControl`.

### Access control

`RouteUtil` implements Flow's `FileRouterRequestUtil`, which Flow's Spring
Security `RequestUtil` calls to decide whether a request for a client view is
anonymous, needs a login, or needs a role. It matches the request path against
the client views, converted to Ant patterns (an optional parameter yields a
pattern with and one without the segment). It reads the views once, or on
every request in `DEVELOPMENT_FRONTEND_LIVERELOAD` mode, and skips requests
without a Vaadin servlet context, such as Spring Boot's management port.
