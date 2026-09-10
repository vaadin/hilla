import { _getPropertyModel, makeObjectEmptyValueCreator, NumberModel, StringModel } from "@vaadin/hilla-lit-form";
import AbstractEntityModel from "./AbstractEntityModel.js";
import type Address from "./Address.js";
class AddressModel<T extends Address = Address> extends AbstractEntityModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(AddressModel);
    get id(): NumberModel {
        return this[_getPropertyModel]("id", (parent, key) => new NumberModel(parent, key, true, { meta: { javaType: "java.lang.Long" } }));
    }
    get street(): StringModel {
        return this[_getPropertyModel]("street", (parent, key) => new StringModel(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get zipCode(): StringModel {
        return this[_getPropertyModel]("zipCode", (parent, key) => new StringModel(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get city(): StringModel {
        return this[_getPropertyModel]("city", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default AddressModel;
