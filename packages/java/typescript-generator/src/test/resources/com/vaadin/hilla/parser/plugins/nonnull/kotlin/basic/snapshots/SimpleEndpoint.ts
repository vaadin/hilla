import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type Employee from "./com/vaadin/hilla/parser/plugins/nonnull/kotlin/basic/data/Employee.js";
import type Manager from "./com/vaadin/hilla/parser/plugins/nonnull/kotlin/basic/data/Manager.js";
import type Person from "./com/vaadin/hilla/parser/plugins/nonnull/kotlin/basic/data/Person.js";
import type Team from "./com/vaadin/hilla/parser/plugins/nonnull/kotlin/basic/data/Team.js";
import type NestedPerson from "./com/vaadin/hilla/parser/plugins/nonnull/kotlin/basic/SimpleEndpoint/NestedPerson.js";
import client from "./connect-client.default.js";
async function getPersonsByFirstname(firstname: string, init?: EndpointRequestInit): Promise<Array<Person>> { return client.call("SimpleEndpoint", "getPersonsByFirstname", { firstname }, init); }
async function isValidPerson(person: Person, init?: EndpointRequestInit): Promise<boolean | undefined> { return client.call("SimpleEndpoint", "isValidPerson", { person }, init); }
async function saveAllPersons(persons: Array<Person>, init?: EndpointRequestInit): Promise<Array<Person | undefined> | undefined> { return client.call("SimpleEndpoint", "saveAllPersons", { persons }, init); }
async function saveCompaniesPerCities(data: Record<string, Record<string, Array<Record<string, Array<Team<Employee, Manager | undefined>> | undefined>>>>, init?: EndpointRequestInit): Promise<Record<string, Record<string, Array<Record<string, Array<Team<Employee | undefined, Manager> | undefined>>>> | undefined>> { return client.call("SimpleEndpoint", "saveCompaniesPerCities", { data }, init); }
async function saveDepartmentManager(data: Record<string, Manager | undefined>, init?: EndpointRequestInit): Promise<Record<string, Manager> | undefined> { return client.call("SimpleEndpoint", "saveDepartmentManager", { data }, init); }
async function saveNestedPerson(person: NestedPerson, init?: EndpointRequestInit): Promise<NestedPerson> { return client.call("SimpleEndpoint", "saveNestedPerson", { person }, init); }
async function saveTeam(team: Team<Employee, Manager | undefined> | undefined, init?: EndpointRequestInit): Promise<Team<Employee, Manager | undefined> | undefined> { return client.call("SimpleEndpoint", "saveTeam", { team }, init); }
async function saveTeams(teams: Array<Team<Employee | undefined, Manager> | undefined> | undefined, init?: EndpointRequestInit): Promise<Array<Team<Employee | undefined, Manager> | undefined>> { return client.call("SimpleEndpoint", "saveTeams", { teams }, init); }
async function sayHello(name: string | undefined, age: number, init?: EndpointRequestInit): Promise<string> { return client.call("SimpleEndpoint", "sayHello", { name, age }, init); }
export { getPersonsByFirstname, isValidPerson, saveAllPersons, saveCompaniesPerCities, saveDepartmentManager, saveNestedPerson, saveTeam, saveTeams, sayHello };
