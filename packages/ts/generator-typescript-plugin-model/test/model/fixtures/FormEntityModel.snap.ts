import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1 } from "@hilla/form";
import FormArrayTypesModel_1 from "./FormArrayTypesModel.js";
import FormDataPrimitivesModel_1 from "./FormDataPrimitivesModel.js";
import type FormEntity_1 from "./FormEntity.js";
import FormEntityHierarchyModel_1 from "./FormEntityHierarchyModel.js";
import FormEntityIdModel_1 from "./FormEntityIdModel.js";
import FormEnumTypesModel_1 from "./FormEnumTypesModel.js";
import FormNonnullTypesModel_1 from "./FormNonnullTypesModel.js";
import FormOptionalTypesModel_1 from "./FormOptionalTypesModel.js";
import FormRecordTypesModel_1 from "./FormRecordTypesModel.js";
import FormTemporalTypesModel_1 from "./FormTemporalTypesModel.js";
import FormValidationConstraintsModel_1 from "./FormValidationConstraintsModel.js";
class FormEntityModel<T extends FormEntity_1 = FormEntity_1> extends FormEntityIdModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(FormEntityModel);
    get myId(): NumberModel_1 {
        return this[_getPropertyModel_1]("myId", (parent, key) => new NumberModel_1(parent, key, false));
    }
    get dataPrimitives(): FormDataPrimitivesModel_1 {
        return this[_getPropertyModel_1]("dataPrimitives", (parent, key) => new FormDataPrimitivesModel_1(parent, key, false));
    }
    get entityHierarchy(): FormEntityHierarchyModel_1 {
        return this[_getPropertyModel_1]("entityHierarchy", (parent, key) => new FormEntityHierarchyModel_1(parent, key, false));
    }
    get temporalTypes(): FormTemporalTypesModel_1 {
        return this[_getPropertyModel_1]("temporalTypes", (parent, key) => new FormTemporalTypesModel_1(parent, key, false));
    }
    get arrayTypes(): FormArrayTypesModel_1 {
        return this[_getPropertyModel_1]("arrayTypes", (parent, key) => new FormArrayTypesModel_1(parent, key, false));
    }
    get enumTypes(): FormEnumTypesModel_1 {
        return this[_getPropertyModel_1]("enumTypes", (parent, key) => new FormEnumTypesModel_1(parent, key, false));
    }
    get recordTypes(): FormRecordTypesModel_1 {
        return this[_getPropertyModel_1]("recordTypes", (parent, key) => new FormRecordTypesModel_1(parent, key, false));
    }
    get validationConstraints(): FormValidationConstraintsModel_1 {
        return this[_getPropertyModel_1]("validationConstraints", (parent, key) => new FormValidationConstraintsModel_1(parent, key, false));
    }
    get myOptionalTypes(): FormOptionalTypesModel_1 {
        return this[_getPropertyModel_1]("myOptionalTypes", (parent, key) => new FormOptionalTypesModel_1(parent, key, false));
    }
    get nonnullTypes(): FormNonnullTypesModel_1 {
        return this[_getPropertyModel_1]("nonnullTypes", (parent, key) => new FormNonnullTypesModel_1(parent, key, false));
    }
    get unknownModel(): ObjectModel_1 {
        return this[_getPropertyModel_1]("unknownModel", (parent, key) => new ObjectModel_1(parent, key, true));
    }
}
export default FormEntityModel;
