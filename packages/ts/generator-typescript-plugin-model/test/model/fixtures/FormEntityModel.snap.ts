import { _getPropertyModel as _getPropertyModel_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1 } from "@hilla/form";
import FormAnnotationsModel_1 from "./FormAnnotationsModel";
import FormArrayTypesModel_1 from "./FormArrayTypesModel";
import FormDataPrimitivesModel_1 from "./FormDataPrimitivesModel";
import type FormEntity_1 from "./FormEntity";
import FormEntityHierarchyModel_1 from "./FormEntityHierarchyModel";
import FormEntityIdModel_1 from "./FormEntityIdModel";
import FormEnumTypesModel_1 from "./FormEnumTypesModel";
import FormNonnullTypesModel_1 from "./FormNonnullTypesModel";
import FormOptionalTypesModel_1 from "./FormOptionalTypesModel";
import FormRecordTypesModel_1 from "./FormRecordTypesModel";
import FormTemporalTypesModel_1 from "./FormTemporalTypesModel";
import FormValidationConstraintsModel_1 from "./FormValidationConstraintsModel";
class FormEntityModel<T extends FormEntity_1 = FormEntity_1> extends FormEntityIdModel_1<T> {
    declare static createEmptyValue: () => FormEntity_1;
    get myId(): NumberModel_1 {
        return this[_getPropertyModel_1]("myId", NumberModel_1, [false]) as NumberModel_1;
    }
    get dataPrimitives(): FormDataPrimitivesModel_1 {
        return this[_getPropertyModel_1]("dataPrimitives", FormDataPrimitivesModel_1, [false]) as FormDataPrimitivesModel_1;
    }
    get entityHierarchy(): FormEntityHierarchyModel_1 {
        return this[_getPropertyModel_1]("entityHierarchy", FormEntityHierarchyModel_1, [false]) as FormEntityHierarchyModel_1;
    }
    get temporalTypes(): FormTemporalTypesModel_1 {
        return this[_getPropertyModel_1]("temporalTypes", FormTemporalTypesModel_1, [false]) as FormTemporalTypesModel_1;
    }
    get arrayTypes(): FormArrayTypesModel_1 {
        return this[_getPropertyModel_1]("arrayTypes", FormArrayTypesModel_1, [false]) as FormArrayTypesModel_1;
    }
    get enumTypes(): FormEnumTypesModel_1 {
        return this[_getPropertyModel_1]("enumTypes", FormEnumTypesModel_1, [false]) as FormEnumTypesModel_1;
    }
    get recordTypes(): FormRecordTypesModel_1 {
        return this[_getPropertyModel_1]("recordTypes", FormRecordTypesModel_1, [false]) as FormRecordTypesModel_1;
    }
    get annotations(): FormAnnotationsModel_1 {
        return this[_getPropertyModel_1]("annotations", FormAnnotationsModel_1, [false]) as FormAnnotationsModel_1;
    }
    get validationConstraints(): FormValidationConstraintsModel_1 {
        return this[_getPropertyModel_1]("validationConstraints", FormValidationConstraintsModel_1, [false]) as FormValidationConstraintsModel_1;
    }
    get myOptionalTypes(): FormOptionalTypesModel_1 {
        return this[_getPropertyModel_1]("myOptionalTypes", FormOptionalTypesModel_1, [false]) as FormOptionalTypesModel_1;
    }
    get nonnullTypes(): FormNonnullTypesModel_1 {
        return this[_getPropertyModel_1]("nonnullTypes", FormNonnullTypesModel_1, [false]) as FormNonnullTypesModel_1;
    }
    get unknownModel(): ObjectModel_1<unknown> {
        return this[_getPropertyModel_1]("unknownModel", ObjectModel_1, [true]) as ObjectModel_1<unknown>;
    }
}
export default FormEntityModel;
