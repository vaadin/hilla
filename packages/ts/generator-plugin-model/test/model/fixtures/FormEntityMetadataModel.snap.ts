import { _getPropertyModel, ArrayModel, makeObjectEmptyValueCreator, NumberModel, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type FormEntityMetadata from "./FormEntityMetadata.js";
import FormEntityModel from "./FormEntityModel.js";
class FormEntityMetadataModel<T extends FormEntityMetadata = FormEntityMetadata> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(FormEntityMetadataModel);
    get withoutMetadata(): StringModel {
        return this[_getPropertyModel]("withoutMetadata", (parent, key) => new StringModel(parent, key, false));
    }
    get withJavaType(): StringModel {
        return this[_getPropertyModel]("withJavaType", (parent, key) => new StringModel(parent, key, false, { meta: { javaType: "java.time.LocalDateTime" } }));
    }
    get listWithJavaType(): ArrayModel<StringModel> {
        return this[_getPropertyModel]("listWithJavaType", (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.time.LocalDateTime" } }), { meta: { javaType: "java.util.List" } }));
    }
    get withAnnotations(): NumberModel {
        return this[_getPropertyModel]("withAnnotations", (parent, key) => new NumberModel(parent, key, false, { meta: { annotations: [{ name: "jakarta.persistence.Id" }, { name: "jakarta.persistence.Version" }] } }));
    }
    get listWithAnnotations(): ArrayModel<NumberModel> {
        return this[_getPropertyModel]("listWithAnnotations", (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new NumberModel(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.Id" }, { name: "jakarta.persistence.Version" }] } }), { meta: { javaType: "java.util.List" } }));
    }
    get withAll(): NumberModel {
        return this[_getPropertyModel]("withAll", (parent, key) => new NumberModel(parent, key, false, { meta: { annotations: [{ name: "jakarta.persistence.Id" }, { name: "jakarta.persistence.Version" }], javaType: "java.lang.Long" } }));
    }
    get nestedModelWithAnnotations(): FormEntityModel {
        return this[_getPropertyModel]("nestedModelWithAnnotations", (parent, key) => new FormEntityModel(parent, key, false, { meta: { annotations: [{ name: "jakarta.persistence.OneToOne" }] } }));
    }
}
export default FormEntityMetadataModel;
