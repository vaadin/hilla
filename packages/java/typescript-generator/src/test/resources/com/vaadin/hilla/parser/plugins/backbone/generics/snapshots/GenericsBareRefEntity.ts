import type GenericsBareRefEntity_1 from "./GenericsBareRefEntity.js";
interface GenericsBareRefEntity<T = unknown> {
    bareGenericTypeProperty?: T;
    bareRefEntityProperty?: GenericsBareRefEntity_1<T>;
}
export default GenericsBareRefEntity;
