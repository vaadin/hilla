import type Entity_1 from "./Entity.js";
interface Container<Entity = unknown> {
    item: Entity;
    owner: Entity_1;
}
export default Container;
