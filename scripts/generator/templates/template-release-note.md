Hilla {{version}}
[Website](https://hilla.dev) · [Getting Started](https://hilla.dev/docs/react/start/quick) · [Documentation](https://hilla.dev/docs/)

## New and Noteworthy since Previous Hilla Update

Hilla 2.2 introduces a form hook for React with server-side validation, and improved hot reloading for faster development.

### New React form support

This release includes a new feature that simplifies the process of building React forms:

1. You define validation rules on your Java object
2. Hilla generates a TypeScript model description that includes the validation logic
3. Use the generated model description to create a form in React
4. The user sees validation errors as soon as they type
5. When the user submits the form, Hilla re-validates the data on the server

Read more about bunding data to forms in the [docs](https://hilla.dev/docs/react/guides/forms/binder).

### Faster development with hot reloading

This release also includes a new feature that addresses an enhancement requested by community members working on large projects: [improved support for hot reloading in development mode](https://github.com/vaadin/hilla/pull/1146).
Now, Hilla endpoints are re-generated without a server restart if you use JRebel or HotSwapAgent, so you can stay in the flow while developing your app.

## Known Issues

## Versions

- Hilla Maven Plugin
- Hilla Gradle Plugin
- Hilla Spring Boot Starter
- Vaadin Design System / Web Components ([{{core.avatar.jsVersion}}](https://github.com/vaadin/web-components/releases/tag/v{{core.avatar.jsVersion}}))
- Vaadin Design System / React Components ([{{react.react-components.jsVersion}}](https://github.com/vaadin/react-components/releases/tag/v{{react.react-components.jsVersion}}))

<!-- Add the What Changed section by GITHUB provided functions  -->

## Supported technologies

<table>
<tr>
  <th>Spring Boot</th>
  <td>Version 3.1 or newer
  </td>
</tr>
<tr>
  <th>Desktop browser</th>
  <td>

- Chrome (evergreen)
- Firefox (evergreen)
  - Firefox Extended Support Release (ESR)
- Safari 15 or newer
- Edge (Chromium, evergreen)
  </td>
</tr>
<tr>
  <th>Mobile browser</th>
  <td>

- Chrome (evergreen) for Android (4.4 or newer)
- Safari for iOS (15 or newer)
  </td>
</tr>
<tr>
  <th>Development OS</th>
  <td>

- Windows
- macOS
- Linux
</td>
</tr>
<tr>
  <th>IDE</th>
  <td>

Any IDE or editor that works with the language of your choice should work well. Our teams often use Eclipse, IntelliJ, VS Code, Atom, Emacs, and Vim, among others.

Vaadin Designer supports the following IDEs:
- Eclipse from Photon and upwards
- JetBrains IntelliJ IDEA from 2017 upwards
  </td>
</tr>
<tr>
  <th>Java</th>
  <td>Version 17 of any JDK or JRE</td>
</tr>
<tr>
  <th>Maven</th>
  <td>Version 3.5 or newer</td>
</tr>
<tr>
  <th>Gradle</th>
  <td>Version 7.6 or newer</td>
</tr>
<tr>
  <th>Node.js</th>
  <td>Version 18 or newer</td>
</tr>
</table>
