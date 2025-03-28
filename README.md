<div align="center">
<img src="hilla-logo.svg" style="width: 6em;">
<h1>Hilla</h1>

The modern web framework
for Java

![Latest Stable Version](https://img.shields.io/npm/v/@vaadin/hilla-frontend.svg)
[![Releases](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fcom%2Fvaadin%2Fhilla%2Fmaven-metadata.xml)](https://github.com/vaadin/hilla/releases)
  
[Website](https://vaadin.com/hilla) · [Docs](https://vaadin.com/docs/latest/hilla) · [Forum](https://vaadin.com/forum/c/hilla/18)

</div>

---

Hilla integrates a Spring Boot Java backend with a reactive TypeScript front end. It helps you build apps faster with type-safe server communication, included UI components, and integrated tooling.

## Simple type-safe server communication

Hilla helps you access the backend easily with type-safe endpoints.

`index.ts`

```ts
// Type info is automatically generated based on Java
import Person from 'Frontend/generated/com/vaadin/hilla/demo/entity/Person';
import { PersonService } from 'Frontend/generated/endpoints';

async function getPeopleWithPhoneNumber() {
  const people: Person[] = await PersonService.findAll();

  // Compile error: The property is 'phone', not 'phoneNumber'
  return people.filter((person) => !!person.phoneNumber);
}

console.log('People with phone numbers: ', getPeopleWithPhoneNumber());
```

`PersonService.java`

```java
@BrowserCallable
@AnonymousAllowed
public class PersonService {

    private PersonRepository repository;

    public PersonService(PersonRepository repository) {
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

Learn more at [vaadin.com/hilla](https://vaadin.com/hilla)

## Get started

Follow the tutorials at https://vaadin.com/docs/latest/getting-started

## Contributing

The best way to contribute is to try out Hilla and provide feedback to the development team in our [Forum](https://vaadin.com/forum/c/hilla/18) or with [GitHub issues](https://github.com/vaadin/hilla/issues).

### Development

If you want to develop Hilla, you can clone the repo and run tests using the following commands:

```sh
git clone https://github.com/vaadin/hilla.git
npm install
npm run build
mvn install -DskipTests
npm test
mvn verify
```

You need the following versions of SDKs and tools:

- **Node.js**: `>= 22 LTS`,
- **npm**: `>=10` (`package-lock.json` is of version 3)
- **JDK**: `>=17`
- **Maven**: `>=3`

---

[![Validation](https://github.com/vaadin/hilla/actions/workflows/validation.yml/badge.svg)](https://github.com/vaadin/hilla/actions/workflows/validation.yml)
[![codecov](https://codecov.io/gh/vaadin/hilla/branch/main/graph/badge.svg?token=PQMTMS8ECC)](https://codecov.io/gh/vaadin/hilla)
