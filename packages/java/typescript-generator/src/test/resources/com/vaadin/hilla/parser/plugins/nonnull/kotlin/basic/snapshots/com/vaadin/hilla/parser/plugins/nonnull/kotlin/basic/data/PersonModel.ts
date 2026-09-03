import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, BooleanModel as BooleanModel_1, Email as Email_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NotBlank as NotBlank_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import AbstractEntityModel_1 from "./AbstractEntityModel.js";
import AddressModel_1 from "./AddressModel.js";
import type Employee_1 from "./Employee.js";
import type Person_1 from "./Person.js";
class PersonModel<T extends Person_1 = Person_1> extends AbstractEntityModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(PersonModel);
    get firstName(): StringModel_1 {
        return this[_getPropertyModel_1]("firstName", (parent, key) => new StringModel_1(parent, key, false, { validators: [new NotBlank_1()], meta: { javaType: "java.lang.String" } }));
    }
    get lastName(): StringModel_1 {
        return this[_getPropertyModel_1]("lastName", (parent, key) => new StringModel_1(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get email(): StringModel_1 {
        return this[_getPropertyModel_1]("email", (parent, key) => new StringModel_1(parent, key, true, { validators: [new Email_1()], meta: { javaType: "java.lang.String" } }));
    }
    get phone(): StringModel_1 {
        return this[_getPropertyModel_1]("phone", (parent, key) => new StringModel_1(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get address(): ArrayModel_1<AddressModel_1> {
        return this[_getPropertyModel_1]("address", (parent, key) => new ArrayModel_1(parent, key, false, (parent, key) => new AddressModel_1(parent, key, false), { meta: { annotations: [{ name: "jakarta.persistence.OneToMany" }], javaType: "java.util.List" } }));
    }
    get team(): ObjectModel_1<Record<string, ReadonlyArray<Employee_1 | undefined>>> {
        return this[_getPropertyModel_1]("team", (parent, key) => new ObjectModel_1(parent, key, false, { meta: { javaType: "java.util.Map" } }));
    }
    get dateOfBirth(): StringModel_1 {
        return this[_getPropertyModel_1]("dateOfBirth", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.time.LocalDate" } }));
    }
    get occupation(): StringModel_1 {
        return this[_getPropertyModel_1]("occupation", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get role(): StringModel_1 {
        return this[_getPropertyModel_1]("role", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get important(): BooleanModel_1 {
        return this[_getPropertyModel_1]("important", (parent, key) => new BooleanModel_1(parent, key, false, { meta: { javaType: "boolean" } }));
    }
}
export default PersonModel;
