import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel } from "@vaadin/hilla-lit-form";
import type ExposedSuperclassEntity from "./ExposedSuperclassEntity.js";
class ExposedSuperclassEntityModel<T extends ExposedSuperclassEntity = ExposedSuperclassEntity> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(ExposedSuperclassEntityModel);
}
export default ExposedSuperclassEntityModel;
