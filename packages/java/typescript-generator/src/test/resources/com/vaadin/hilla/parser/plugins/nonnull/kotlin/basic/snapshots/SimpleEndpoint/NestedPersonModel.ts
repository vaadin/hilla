import { _getPropertyModel, ArrayModel, BooleanModel, Email, makeObjectEmptyValueCreator, NotBlank, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import AddressModel from "../data/AddressModel.js";
import type Employee from "../data/Employee.js";
import type NestedPerson from "./NestedPerson.js";
class NestedPersonModel<T extends NestedPerson = NestedPerson> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(NestedPersonModel);
    get firstName(): StringModel {
        return this[_getPropertyModel]("firstName", (parent, key) => new StringModel(parent, key, false, { validators: [new NotBlank()], meta: { javaType: "java.lang.String" } }));
    }
    get lastName(): StringModel {
        return this[_getPropertyModel]("lastName", (parent, key) => new StringModel(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get email(): StringModel {
        return this[_getPropertyModel]("email", (parent, key) => new StringModel(parent, key, true, { validators: [new Email()], meta: { javaType: "java.lang.String" } }));
    }
    get phone(): StringModel {
        return this[_getPropertyModel]("phone", (parent, key) => new StringModel(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get address(): ArrayModel<AddressModel> {
        return this[_getPropertyModel]("address", (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new AddressModel(parent, key, false), { meta: { javaType: "java.util.List" } }));
    }
    get team(): ObjectModel<Record<string, ReadonlyArray<Employee | undefined>>> {
        return this[_getPropertyModel]("team", (parent, key) => new ObjectModel(parent, key, false, { meta: { javaType: "java.util.Map" } }));
    }
    get dateOfBirth(): StringModel {
        return this[_getPropertyModel]("dateOfBirth", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.time.LocalDate" } }));
    }
    get occupation(): StringModel {
        return this[_getPropertyModel]("occupation", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get role(): StringModel {
        return this[_getPropertyModel]("role", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get important(): BooleanModel {
        return this[_getPropertyModel]("important", (parent, key) => new BooleanModel(parent, key, false, { meta: { javaType: "boolean" } }));
    }
}
export default NestedPersonModel;
