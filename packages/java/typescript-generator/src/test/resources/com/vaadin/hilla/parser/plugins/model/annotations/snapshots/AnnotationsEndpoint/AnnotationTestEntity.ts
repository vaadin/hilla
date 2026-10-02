import type NestedEntity from "./NestedEntity.js";
interface AnnotationTestEntity {
    id?: number;
    version?: number;
    oneToOne?: NestedEntity;
    oneToMany?: Array<NestedEntity | undefined>;
    manyToOne?: NestedEntity;
    manyToMany?: Array<NestedEntity | undefined>;
    manyToManyWithFetchType?: Array<NestedEntity | undefined>;
    name?: string;
}
export default AnnotationTestEntity;
