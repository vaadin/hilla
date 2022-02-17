<div align="center">
<img src="hilla-logo.svg" style="width: 6em;">
<h1>Hilla</h1>

The modern web framework
for Java

![Latest Stable Version](https://img.shields.io/npm/v/@hilla/frontend.svg)
[![Releases](https://img.shields.io/badge/maven-v1.0.0.beta2-1d77e4)](https://github.com/vaadin/fusion/releases)
  
[hilla.dev](https://hilla.dev) · [Docs](https://hilla.dev/docs) · [Chat](https://discord.gg/MYFq5RTbBn)

</div>

---

Hilla integrates a Spring Boot Java backend with a reactive TypeScript front end. It helps you build apps faster with type-safe server communication, included UI components, and integrated tooling.

## Simple type-safe server communication

Hilla helps you access the backend easily with type-safe endpoints.

`index.ts`

```ts
// Type info is automatically generated based on Java
import Person from 'Frontend/generated/dev/hilla/demo/entity/Person';
import { PersonEndpoint } from 'Frontend/generated/endpoints';

async function getPeopleWithPhoneNumber() {
  const people: Person[] = await PersonEndpoint.findAll();

  // Compile error: The property is 'phone', not 'phoneNumber'
  return people.filter((person) => !!person.phoneNumber);
}

console.log('People with phone numbers: ', getPeopleWithPhoneNumber());
```

`PersonEndpoint.java`

```java
@Endpoint
@AnonymousAllowed
public class PersonEndpoint {

    private PersonRepository repository;

    public PersonEndpoint(PersonRepository repository) {
        this.repository = repository;
    }

    public @Nonnull List<@Nonnull Person> findAll() {
        return repository.findAll();
    }
}
```

`Person.java`

```java
@Entity
public class Person {

    @Id
    @GeneratedValue
    private Integer id;

    @Nonnull private String firstName;
    @Nonnull private String lastName;
    @Email @Nonnull private String email;
    @Nonnull private String phone;

    // getters, setters
}
```

Learn more at [hilla.dev](https://hilla.dev)

## Get started

Follow the tutorials at https://hilla.dev/docs/tutorials

## Contributing

The best way to contribute is to try out Hilla and provide feedback to the development team in our [Discord chat](https://discord.gg/MYFq5RTbBn) or with [GitHub issues](https://github.com/vaadin/hilla/issues).

### Development

If you want to develop Hilla, you can clone the repo and run tests using the following commands:

```sh
git clone https://github.com/vaadin/hilla.git
npm install
npm run build
npm test
```

You need the following versions of Node.js and npm:

- **Node.js**: `>= 16.14.0` (native support for ES Modules and NodeJS execution of the newest hooks),
- **npm**: `^7` (`package-lock.json` is of version 2; also, `lerna` is unable to bootstrap this project correctly with lower `npm`)

---

![Frontend CI](https://github.com/vaadin/hilla/actions/workflows/ts.yml/badge.svg)
![Java CI](https://github.com/vaadin/hilla/actions/workflows/java.yml/badge.svg)
[![codecov](https://codecov.io/gh/vaadin/hilla/branch/main/graph/badge.svg?token=PQMTMS8ECC)](https://codecov.io/gh/vaadin/hilla)
