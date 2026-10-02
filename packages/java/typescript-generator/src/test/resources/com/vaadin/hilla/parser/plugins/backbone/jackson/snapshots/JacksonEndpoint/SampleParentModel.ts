import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type SampleParent from "./SampleParent.js";
class SampleParentModel<T extends SampleParent = SampleParent> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(SampleParentModel);
    get publicParentProperty(): StringModel {
        return this[_getPropertyModel]("publicParentProperty", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get privateParentProperty(): StringModel {
        return this[_getPropertyModel]("privateParentProperty", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default SampleParentModel;
