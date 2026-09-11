import { _getPropertyModel, makeObjectEmptyValueCreator, NumberModel, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type User from "./User.js";
class UserModel<T extends User = User> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(UserModel);
    get name(): StringModel {
        return this[_getPropertyModel]("name", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get age(): NumberModel {
        return this[_getPropertyModel]("age", (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: "int" } }));
    }
}
export default UserModel;
