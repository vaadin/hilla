# Internationalization runtime

## Overview

`@vaadin/hilla-react-i18n` translates the strings of client-side views. All
of it is in [`src/index.ts`](src/index.ts): one global `I18n` instance,
`i18n`, the ``key`…` `` tag that marks a string as a translation key, and
`translate`.

```tsx
await i18n.configure();
// in a component:
const title = translate(key`orders.title`);
```

The translations are not part of the bundle. They are the same
`vaadin-i18n/translations*.properties` files that Flow views use, served by
Flow. The runtime fetches them for the current language, keeps them in a
signal, and formats them with ICU MessageFormat. Flow's side of this (the
request handler, the `I18NProvider`, and the build plugin that splits the keys
by chunk) is described in Flow's
`flow-build-tools/src/main/resources/plugins/rollup-plugin-vaadin-i18n/ARCHITECTURE.md`.

The life of the translations goes like this:

1. `configure()` picks the initial language and loads its translations.
2. Components call `translate`, which reads the translations signal. A
   component re-renders when the translations change.
3. In a production build, each lazy chunk that uses keys calls
   `registerChunk` when it loads, and only its keys are fetched.
4. `setLanguage()` loads the new language for everything loaded so far,
   remembers the choice, and the views re-render.

## Design goals

- **One source with Flow.** The server, not the bundle, holds the
  translations, so Flow and Hilla views read the same files, with the same
  locale fallback and the same marker for a missing key.
- **Reactive without a provider component.** The state is in signals from
  `@vaadin/hilla-react-signals`. `translate` reads a signal, so any component
  or effect that calls it updates on its own, with no React context.
- **Load only what is used, in production.** The keys are split by the
  bundle's own chunks at build time, so the application declares nothing.
- **Live in development.** All keys of the language are loaded, and edits
  of the properties files show up without a reload.

## State

`I18n` keeps everything in signals:

- `#language` is the configured language, and `#resolvedLanguage` the value
  of the `X-Vaadin-Retrieved-Locale` response header. The server sends the
  requested language there, so the two are the same: the server does not
  report which bundle the translations came from.
- `#translations` maps keys to message strings.
- `#initialized` becomes `true` after the first load. An application can show
  a placeholder until then.
- `#alreadyRequestedKeys` holds the keys requested one by one, so a key that
  the server doesn't have is requested only once.

All of a load's updates are set inside one `batch()`, so a language change
re-renders once. `FormatCache` keeps one `IntlMessageFormat` per message for
the current language, and is replaced when the language changes.

## Choosing the language

`configure({ language })` uses, in this order: the language given, the
language saved by an earlier `setLanguage`, and `navigator.language`. The
saved language is kept in the `vaadinLanguageSettings` cookie as JSON
([`src/settings.ts`](src/settings.ts)).
`setLanguage` saves it, and `configure` does not.

## Loading

All loads go through `updateLanguage` and `DefaultBackend.loadTranslations`
([`src/backend.ts`](src/backend.ts)). That sends
`GET ./?v-r=i18n&langtag=<language>`, with repeated `chunks=` and `keys=`
parameters. What a load asks for depends on why it runs:

| Trigger                                                             | Request                     | Merge   |
| ------------------------------------------------------------------- | --------------------------- | ------- |
| `configure`, `setLanguage`, no chunks registered (development mode) | the language only: all keys | replace |
| `configure`, `setLanguage`, chunks registered (production)          | every registered chunk      | replace |
| `registerChunk(name)`, language already set                         | that chunk                  | merge   |
| `translateDynamic` with a key that isn't loaded                     | `keys=` of the batched keys | merge   |
| `translations-update` HMR event (development)                       | the language only           | replace |

- **Registered chunks.** `registerChunk` is not called by application code.
  Flow's production build prepends `await i18n.registerChunk('<chunk file>')`
  to every chunk that uses keys. A chunk that registers before `configure` is
  only recorded, and the first `configure` loads it with the others.
- **Batched keys.** `translateDynamic(key)` returns a computed signal. For a
  key that isn't loaded yet, it gives an empty string and calls
  `requestKeys`, which collects all keys requested in the same microtask into
  one request and logs a warning. In production, such a key was missed by the
  build, usually because it isn't a ``key`literal` `` inside `translate(…)`.
- **Failures.** A failed load is logged and leaves the current translations
  in place.

## Missing translations

`translate` returns `!<language>: <key>` for a key without a translation,
with the language part only (`!de: orders.title`). Flow's
`DefaultI18NProvider` uses the same format, so a missing key looks the same in
both kinds of views.

## Working on it

- Unit tests: [`test/i18n.spec.tsx`](test/i18n.spec.tsx), with a mocked
  `fetch`.
- Integration tests: `packages/java/tests/spring/react-i18n` (`BasicI18NIT`
  for languages and fallbacks, `ChunksIT` for chunked loading in a
  production build).
- The `?v-r=i18n` request, its parameters and the response header are a
  contract with Flow's `TranslationFileRequestHandler`. Change both together.
