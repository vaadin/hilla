import { _getPropertyModel, makeObjectEmptyValueCreator, NumberModel, ObjectModel } from "@vaadin/hilla-lit-form";
import FormArrayTypesModel from "./FormArrayTypesModel.js";
import FormDataPrimitivesModel from "./FormDataPrimitivesModel.js";
import type FormEntity from "./FormEntity.js";
import FormEntityHierarchyModel from "./FormEntityHierarchyModel.js";
import FormEntityIdModel from "./FormEntityIdModel.js";
import FormEnumTypesModel from "./FormEnumTypesModel.js";
import FormNonnullTypesModel from "./FormNonnullTypesModel.js";
import FormOptionalTypesModel from "./FormOptionalTypesModel.js";
import FormRecordTypesModel from "./FormRecordTypesModel.js";
import FormTemporalTypesModel from "./FormTemporalTypesModel.js";
import FormValidationConstraintsModel from "./FormValidationConstraintsModel.js";
class FormEntityModel<T extends FormEntity = FormEntity> extends FormEntityIdModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(FormEntityModel);
    get myId(): NumberModel {
        return this[_getPropertyModel]("myId", (parent, key) => new NumberModel(parent, key, false));
    }
    get dataPrimitives(): FormDataPrimitivesModel {
        return this[_getPropertyModel]("dataPrimitives", (parent, key) => new FormDataPrimitivesModel(parent, key, false));
    }
    get entityHierarchy(): FormEntityHierarchyModel {
        return this[_getPropertyModel]("entityHierarchy", (parent, key) => new FormEntityHierarchyModel(parent, key, false));
    }
    get temporalTypes(): FormTemporalTypesModel {
        return this[_getPropertyModel]("temporalTypes", (parent, key) => new FormTemporalTypesModel(parent, key, false));
    }
    get arrayTypes(): FormArrayTypesModel {
        return this[_getPropertyModel]("arrayTypes", (parent, key) => new FormArrayTypesModel(parent, key, false));
    }
    get enumTypes(): FormEnumTypesModel {
        return this[_getPropertyModel]("enumTypes", (parent, key) => new FormEnumTypesModel(parent, key, false));
    }
    get recordTypes(): FormRecordTypesModel {
        return this[_getPropertyModel]("recordTypes", (parent, key) => new FormRecordTypesModel(parent, key, false));
    }
    get validationConstraints(): FormValidationConstraintsModel {
        return this[_getPropertyModel]("validationConstraints", (parent, key) => new FormValidationConstraintsModel(parent, key, false));
    }
    get myOptionalTypes(): FormOptionalTypesModel {
        return this[_getPropertyModel]("myOptionalTypes", (parent, key) => new FormOptionalTypesModel(parent, key, false));
    }
    get nonnullTypes(): FormNonnullTypesModel {
        return this[_getPropertyModel]("nonnullTypes", (parent, key) => new FormNonnullTypesModel(parent, key, false));
    }
    get unknownModel(): ObjectModel {
        return this[_getPropertyModel]("unknownModel", (parent, key) => new ObjectModel(parent, key, true));
    }
}
export default FormEntityModel;
