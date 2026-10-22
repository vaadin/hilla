import type GenericsBareRefEntity from "./GenericsBareRefEntity.js";
interface GenericsExtendedRefEntity {
    extendedGenericTypeReference?: GenericsBareRefEntity<string | undefined>;
}
export default GenericsExtendedRefEntity;
