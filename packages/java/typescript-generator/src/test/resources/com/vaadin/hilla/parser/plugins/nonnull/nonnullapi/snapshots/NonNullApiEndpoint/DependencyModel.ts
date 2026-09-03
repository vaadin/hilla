import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type Dependency_1 from "./Dependency.js";
class DependencyModel<T extends Dependency_1 = Dependency_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(DependencyModel);
    get defaultField(): StringModel_1 {
        return this[_getPropertyModel_1]("defaultField", (parent, key) => new StringModel_1(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get nullableField(): StringModel_1 {
        return this[_getPropertyModel_1]("nullableField", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get nullableSignatureField(): StringModel_1 {
        return this[_getPropertyModel_1]("nullableSignatureField", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default DependencyModel;
