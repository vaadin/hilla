import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type Employee_1 from "./com/vaadin/hilla/parser/plugins/nonnull/kotlin/basic/data/Employee.js";
import type Manager_1 from "./com/vaadin/hilla/parser/plugins/nonnull/kotlin/basic/data/Manager.js";
import type Person_1 from "./com/vaadin/hilla/parser/plugins/nonnull/kotlin/basic/data/Person.js";
import type Team_1 from "./com/vaadin/hilla/parser/plugins/nonnull/kotlin/basic/data/Team.js";
import type NestedPerson_1 from "./com/vaadin/hilla/parser/plugins/nonnull/kotlin/basic/SimpleEndpoint/NestedPerson.js";
import client_1 from "./connect-client.default.js";
async function getPersonsByFirstname_1(firstname: string, init?: EndpointRequestInit_1): Promise<Array<Person_1>> { return client_1.call("SimpleEndpoint", "getPersonsByFirstname", { firstname }, init); }
async function isValidPerson_1(person: Person_1, init?: EndpointRequestInit_1): Promise<boolean | undefined> { return client_1.call("SimpleEndpoint", "isValidPerson", { person }, init); }
async function saveAllPersons_1(persons: Array<Person_1>, init?: EndpointRequestInit_1): Promise<Array<Person_1 | undefined> | undefined> { return client_1.call("SimpleEndpoint", "saveAllPersons", { persons }, init); }
async function saveCompaniesPerCities_1(data: Record<string, Record<string, Array<Record<string, Array<Team_1<Employee_1, Manager_1 | undefined>> | undefined>>>>, init?: EndpointRequestInit_1): Promise<Record<string, Record<string, Array<Record<string, Array<Team_1<Employee_1 | undefined, Manager_1> | undefined>>>> | undefined>> { return client_1.call("SimpleEndpoint", "saveCompaniesPerCities", { data }, init); }
async function saveDepartmentManager_1(data: Record<string, Manager_1 | undefined>, init?: EndpointRequestInit_1): Promise<Record<string, Manager_1> | undefined> { return client_1.call("SimpleEndpoint", "saveDepartmentManager", { data }, init); }
async function saveNestedPerson_1(person: NestedPerson_1, init?: EndpointRequestInit_1): Promise<NestedPerson_1> { return client_1.call("SimpleEndpoint", "saveNestedPerson", { person }, init); }
async function saveTeam_1(team: Team_1<Employee_1, Manager_1 | undefined> | undefined, init?: EndpointRequestInit_1): Promise<Team_1<Employee_1, Manager_1 | undefined> | undefined> { return client_1.call("SimpleEndpoint", "saveTeam", { team }, init); }
async function saveTeams_1(teams: Array<Team_1<Employee_1 | undefined, Manager_1> | undefined> | undefined, init?: EndpointRequestInit_1): Promise<Array<Team_1<Employee_1 | undefined, Manager_1> | undefined>> { return client_1.call("SimpleEndpoint", "saveTeams", { teams }, init); }
async function sayHello_1(name: string | undefined, age: number, init?: EndpointRequestInit_1): Promise<string> { return client_1.call("SimpleEndpoint", "sayHello", { name, age }, init); }
export { getPersonsByFirstname_1 as getPersonsByFirstname, isValidPerson_1 as isValidPerson, saveAllPersons_1 as saveAllPersons, saveCompaniesPerCities_1 as saveCompaniesPerCities, saveDepartmentManager_1 as saveDepartmentManager, saveNestedPerson_1 as saveNestedPerson, saveTeam_1 as saveTeam, saveTeams_1 as saveTeams, sayHello_1 as sayHello };
