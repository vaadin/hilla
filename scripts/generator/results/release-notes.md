Vaadin 2.2-SNAPSHOT

[Changelogs](#_changelogs) · [Upgrading guides](#_upgrading_guides) · [Docs](https://vaadin.com/docs/latest/) · [Get Started](https://vaadin.com/start/)

## New and Noteworthy Since Vaadin 24.1

**Notable Changes**

Gradle support is raised to the version (Gradle 7.6) required by [jackson](https://github.com/FasterXML/jackson-core/issues/955)

### Flow
- 
### Hilla
- 

### Design System
- 

### Collaboration Engine
- 
`*` experimental

## <a id="_changelogs"></a> Changelogs

- Flow ([24.2.0.alpha5](https://github.com/vaadin/flow/releases/tag/24.2.0.alpha5)) and Hilla ([2.2-SNAPSHOT](https://github.com/vaadin/hilla/releases/tag/2.2-SNAPSHOT)) 
- Design System
  - Web Components ([24.2.0-alpha9](https://github.com/vaadin/web-components/releases/tag/v24.2.0-alpha9))
  - Flow Components ([2.2-SNAPSHOT](https://github.com/vaadin/flow-components/releases/tag/2.2-SNAPSHOT))
- Designer ([Release notes](https://github.com/vaadin/designer/blob/master/RELEASE-NOTES.md))
- Design System Publisher ([Documentation](https://vaadin.com/design-system-publisher))
- TestBench ([9.0.5](https://github.com/vaadin/testbench/releases/tag/9.0.5))
- Classic Components([24.1.0](https://github.com/vaadin/classic-components/releases/tag/24.1.0))
- Multiplatform Runtime (MPR) ([7.0.6](https://github.com/vaadin/multiplatform-runtime/releases/tag/7.0.6))
- Router ([1.7.5](https://github.com/vaadin/vaadin-router/releases/tag/v1.7.5))
- Vaadin Kits
  - Azure Kit ([1.0.0](https://vaadin.com/docs/latest/tools/azure))
  - Collaboration Engine ([6.0.0](https://github.com/vaadin/collaboration-engine/releases/tag/6.0.0))
  - Kubernetes Kit ([2.0.0](https://github.com/vaadin/kubernetes-kit/releases/tag/2.0.0))
  - Observability Kit ([2.1.1](https://github.com/vaadin/observability-kit/releases/tag/{{kits.observability-kit.javaVersion}}))
  - SSO Kit ([2.1.1](https://github.com/vaadin/sso-kit/releases/tag/2.1.1))
  - Swing Kit ([2.1.0](https://vaadin.com/docs/latest/tools/swing))
- Designer ([Release notes](https://github.com/vaadin/designer/blob/master/RELEASE-NOTES.md))

**Official add-ons and plugins:**

- Spring add-on ([24.2.0.alpha5](https://github.com/vaadin/flow/releases/tag/24.2.0.alpha5))
- CDI add-on ([15.0.1](https://github.com/vaadin/cdi/releases/tag/15.0.1))
- Maven plugin (2.2-SNAPSHOT)
- Gradle plugin (2.2-SNAPSHOT)
- Quarkus plugin ([2.0.1](https://github.com/vaadin/quarkus/releases/tag/2.0.1))

## <a id="_upgrading_guides"></a> Upgrading guides

- [Upgrading Flow to Vaadin 24](https://vaadin.com/docs/latest/flow/upgrading/changes/#changes-in-vaadin-24)
- [Upgrading Fusion to Vaadin 24](https://vaadin.com/docs/latest/fusion/upgrading/changes/#changes-in-vaadin-24)
- [Upgrading Design System to Vaadin 24](https://vaadin.com/docs/latest/ds/upgrading)



## Support
<!-- New LTS:

Vaadin 24 is the latest stable version, with extended support options available ([release model](https://vaadin.com/roadmap)).

-->

<!-- Non-LTS:

Vaadin 24 is supported for one month after Vaadin 25 has been released ([release model](https://vaadin.com/roadmap)).

-->
Vaadin also provides [commercial support and warranty](https://vaadin.com/solutions/support).



## Supported technologies

<table>
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
  <td>Version 7.3 or newer</td>
</tr>
<tr>
  <th>Application server</th>
  <td>

Vaadin Flow requires Java Servlet API 6 and Java 17 or newer. It is tested on:

- Apache Tomcat 10.1
- Open Liberty 23.0.0.1-beta
- RedHat JBoss EAP 8.0 beta
- WildFly 27
- Jetty 12 beta
- Payara Server 6
- Payara Micro 6
  </td>
</tr>
<tr>
  <th>Node.js</th>
  <td>Version 18 or newer</td>
</tr>
<tr>
  <th>Spring Boot</th>
  <td>Version 3.0 or newer
  </td>
</tr>
</table>



## Known issues and limitations

<table>
<tr>
  <th>Flow</th>
  <td>

- The Template-in-Template feature has [some limitations](https://github.com/vaadin/flow/issues?utf8=%E2%9C%93&q=is%3Aissue+is%3Aopen+label%3Atemplate-in-template+)
  </td>
</tr>
</table>
