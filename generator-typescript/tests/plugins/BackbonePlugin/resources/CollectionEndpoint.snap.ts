import type Collection_1 from "./com/vaadin/fusion/parser/plugins/backbone/collectionendpoint/CollectionEndpoint/Collection.js";
import type client_1 from "./connect-client.default.js";
async function getCollectionByAuthor_1(name: string | undefined): Promise<Collection_1 | undefined> { return client_1.call("CollectionEndpoint", "getCollectionByAuthor", { name }); }
async function getListOfUserName_1(): Promise<Array<string | undefined> | undefined> { return client_1.call("CollectionEndpoint", "getListOfUserName"); }
export { getCollectionByAuthor_1 as getCollectionByAuthor, getListOfUserName_1 as getListOfUserName };
