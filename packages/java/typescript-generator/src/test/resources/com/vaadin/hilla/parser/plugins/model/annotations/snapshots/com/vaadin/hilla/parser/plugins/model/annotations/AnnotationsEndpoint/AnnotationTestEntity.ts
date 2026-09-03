import type NestedEntity_1 from "./NestedEntity.js";
interface AnnotationTestEntity {
    id?: number;
    version?: number;
    oneToOne?: NestedEntity_1;
    oneToMany?: Array<NestedEntity_1 | undefined>;
    manyToOne?: NestedEntity_1;
    manyToMany?: Array<NestedEntity_1 | undefined>;
    manyToManyWithFetchType?: Array<NestedEntity_1 | undefined>;
    name?: string;
}
export default AnnotationTestEntity;
