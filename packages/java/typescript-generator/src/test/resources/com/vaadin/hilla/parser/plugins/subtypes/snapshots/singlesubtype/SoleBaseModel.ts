import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type SoleBase_1 from "./SoleBase.js";
class SoleBaseModel<T extends SoleBase_1 = SoleBase_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(SoleBaseModel);
    get id(): StringModel_1 {
        return this[_getPropertyModel_1]("id", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default SoleBaseModel;
