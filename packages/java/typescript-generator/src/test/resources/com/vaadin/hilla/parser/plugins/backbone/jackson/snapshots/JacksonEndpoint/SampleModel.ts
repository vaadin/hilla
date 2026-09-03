import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type Sample_1 from "./Sample.js";
import SampleParentModel_1 from "./SampleParentModel.js";
class SampleModel<T extends Sample_1 = Sample_1> extends SampleParentModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(SampleModel);
    get publicProp(): StringModel_1 {
        return this[_getPropertyModel_1]("publicProp", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get privateProp(): StringModel_1 {
        return this[_getPropertyModel_1]("privateProp", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get privateTransientPropWithGetter(): StringModel_1 {
        return this[_getPropertyModel_1]("privateTransientPropWithGetter", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get propertyGetterOnly(): StringModel_1 {
        return this[_getPropertyModel_1]("propertyGetterOnly", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get propertyWithDifferentField(): StringModel_1 {
        return this[_getPropertyModel_1]("propertyWithDifferentField", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get propertySetterOnly(): StringModel_1 {
        return this[_getPropertyModel_1]("propertySetterOnly", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get renamedPublicProp0(): StringModel_1 {
        return this[_getPropertyModel_1]("renamedPublicProp0", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get renamedPrivateProp0(): StringModel_1 {
        return this[_getPropertyModel_1]("renamedPrivateProp0", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default SampleModel;
