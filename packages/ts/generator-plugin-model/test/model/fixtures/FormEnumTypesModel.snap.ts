import { _enum, EnumModel, makeEnumEmptyValueCreator } from "@vaadin/hilla-lit-form";
import FormEnumTypes from "./FormEnumTypes.js";
class FormEnumTypesModel extends EnumModel<typeof FormEnumTypes> {
    static override createEmptyValue = makeEnumEmptyValueCreator(FormEnumTypesModel);
    readonly [_enum] = FormEnumTypes;
}
export default FormEnumTypesModel;
