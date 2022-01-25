import { _getPropertyModel as _getPropertyModel_1, NumberModel as NumberModel_1 } from "@hilla/form";
import type FormAnnotations_1 from "./FormAnnotations";
import FormAnnotationsModel_1 from "./FormAnnotationsModel";
import type FormArrayTypes_1 from "./FormArrayTypes";
import FormArrayTypesModel_1 from "./FormArrayTypesModel";
import type FormDataPrimitives_1 from "./FormDataPrimitives";
import FormDataPrimitivesModel_1 from "./FormDataPrimitivesModel";
import type FormEntity_1 from "./FormEntity";
import type FormEntityHierarchy_1 from "./FormEntityHierarchy";
import FormEntityHierarchyModel_1 from "./FormEntityHierarchyModel";
import FormEntityIdModel_1 from "./FormEntityIdModel";
import type FormNonnullTypes_1 from "./FormNonnullTypes";
import FormNonnullTypesModel_1 from "./FormNonnullTypesModel";
import type FormOptionalTypes_1 from "./FormOptionalTypes";
import FormOptionalTypesModel_1 from "./FormOptionalTypesModel";
import type FormRecordTypes_1 from "./FormRecordTypes";
import FormRecordTypesModel_1 from "./FormRecordTypesModel";
import type FormTemporalTypes_1 from "./FormTemporalTypes";
import FormTemporalTypesModel_1 from "./FormTemporalTypesModel";
import type FormValidationConstraints_1 from "./FormValidationConstraints";
import FormValidationConstraintsModel_1 from "./FormValidationConstraintsModel";
class FormEntityModel<T extends FormEntity_1 = FormEntity_1> extends FormEntityIdModel_1<T> {
    static createEmptyValue: () => FormEntity_1;
    get myId(): NumberModel_1 {
        return this[_getPropertyModel_1]("myId", NumberModel_1, [true]) as NumberModel_1;
    }
    get dataPrimitives(): FormDataPrimitivesModel_1 {
        return this[_getPropertyModel_1]("dataPrimitives", FormDataPrimitivesModel_1, [true]) as FormDataPrimitivesModel_1;
    }
    get entityHierarchy(): FormEntityHierarchyModel_1 {
        return this[_getPropertyModel_1]("entityHierarchy", FormEntityHierarchyModel_1, [true]) as FormEntityHierarchyModel_1;
    }
    get temporalTypes(): FormTemporalTypesModel_1 {
        return this[_getPropertyModel_1]("temporalTypes", FormTemporalTypesModel_1, [true]) as FormTemporalTypesModel_1;
    }
    get arrayTypes(): FormArrayTypesModel_1 {
        return this[_getPropertyModel_1]("arrayTypes", FormArrayTypesModel_1, [true]) as FormArrayTypesModel_1;
    }
    get recordTypes(): FormRecordTypesModel_1 {
        return this[_getPropertyModel_1]("recordTypes", FormRecordTypesModel_1, [true]) as FormRecordTypesModel_1;
    }
    get annotations(): FormAnnotationsModel_1 {
        return this[_getPropertyModel_1]("annotations", FormAnnotationsModel_1, [true]) as FormAnnotationsModel_1;
    }
    get validationConstraints(): FormValidationConstraintsModel_1 {
        return this[_getPropertyModel_1]("validationConstraints", FormValidationConstraintsModel_1, [true]) as FormValidationConstraintsModel_1;
    }
    get myOptionalTypes(): FormOptionalTypesModel_1 {
        return this[_getPropertyModel_1]("myOptionalTypes", FormOptionalTypesModel_1, [true]) as FormOptionalTypesModel_1;
    }
    get nonnullTypes(): FormNonnullTypesModel_1 {
        return this[_getPropertyModel_1]("nonnullTypes", FormNonnullTypesModel_1, [true]) as FormNonnullTypesModel_1;
    }
}
export default FormEntityModel;
