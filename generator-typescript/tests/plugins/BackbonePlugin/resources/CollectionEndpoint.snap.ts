import client_1 from "./connect-client.default.js";
import type Collection from "./com/vaadin/fusion/parser/plugins/backbone/collectionendpoint/CollectionEndpoint/Collection";
async function getCollectionByAuthor_1(name: string | undefined): Promise<Collection | undefined> { return client_1.call("CollectionEndpoint", "getCollectionByAuthor", { name }); }
async function getListOfUserName_1(): Promise<Array<string | undefined> | undefined> { return client_1.call("CollectionEndpoint", "getListOfUserName"); }
export { getCollectionByAuthor_1 as getCollectionByAuthor, getListOfUserName_1 as getListOfUserName };
