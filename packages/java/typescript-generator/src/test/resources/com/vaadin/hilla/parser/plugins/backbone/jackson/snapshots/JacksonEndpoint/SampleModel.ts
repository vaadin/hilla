import { _getPropertyModel, makeObjectEmptyValueCreator, StringModel } from "@vaadin/hilla-lit-form";
import type Sample from "./Sample.js";
import SampleParentModel from "./SampleParentModel.js";
class SampleModel<T extends Sample = Sample> extends SampleParentModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(SampleModel);
    get publicProp(): StringModel {
        return this[_getPropertyModel]("publicProp", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get privateProp(): StringModel {
        return this[_getPropertyModel]("privateProp", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get privateTransientPropWithGetter(): StringModel {
        return this[_getPropertyModel]("privateTransientPropWithGetter", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get propertyGetterOnly(): StringModel {
        return this[_getPropertyModel]("propertyGetterOnly", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get propertyWithDifferentField(): StringModel {
        return this[_getPropertyModel]("propertyWithDifferentField", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get propertySetterOnly(): StringModel {
        return this[_getPropertyModel]("propertySetterOnly", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get renamedPublicProp0(): StringModel {
        return this[_getPropertyModel]("renamedPublicProp0", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get renamedPrivateProp0(): StringModel {
        return this[_getPropertyModel]("renamedPrivateProp0", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default SampleModel;
