import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import AbstractEntityModel_1 from "./AbstractEntityModel.js";
import type Address_1 from "./Address.js";
class AddressModel<T extends Address_1 = Address_1> extends AbstractEntityModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(AddressModel);
    get street(): StringModel_1 {
        return this[_getPropertyModel_1]("street", (parent, key) => new StringModel_1(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get zipCode(): StringModel_1 {
        return this[_getPropertyModel_1]("zipCode", (parent, key) => new StringModel_1(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get city(): StringModel_1 {
        return this[_getPropertyModel_1]("city", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default AddressModel;
