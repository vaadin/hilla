import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type SampleParent_1 from "./SampleParent.js";
class SampleParentModel<T extends SampleParent_1 = SampleParent_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(SampleParentModel);
    get publicParentProperty(): StringModel_1 {
        return this[_getPropertyModel_1]("publicParentProperty", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get privateParentProperty(): StringModel_1 {
        return this[_getPropertyModel_1]("privateParentProperty", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default SampleParentModel;
