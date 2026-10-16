import { _getPropertyModel, BooleanModel, makeObjectEmptyValueCreator, NumberModel, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type FormDataPrimitives from "./FormDataPrimitives.js";
class FormDataPrimitivesModel<T extends FormDataPrimitives = FormDataPrimitives> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(FormDataPrimitivesModel);
    get stringProp(): StringModel {
        return this[_getPropertyModel]("stringProp", (parent, key) => new StringModel(parent, key, false));
    }
    get longWrapperProp(): NumberModel {
        return this[_getPropertyModel]("longWrapperProp", (parent, key) => new NumberModel(parent, key, false));
    }
    get longProp(): NumberModel {
        return this[_getPropertyModel]("longProp", (parent, key) => new NumberModel(parent, key, false));
    }
    get integerWrapperProp(): NumberModel {
        return this[_getPropertyModel]("integerWrapperProp", (parent, key) => new NumberModel(parent, key, false));
    }
    get integerProp(): NumberModel {
        return this[_getPropertyModel]("integerProp", (parent, key) => new NumberModel(parent, key, false));
    }
    get doubleWrapperProp(): NumberModel {
        return this[_getPropertyModel]("doubleWrapperProp", (parent, key) => new NumberModel(parent, key, false));
    }
    get doubleProp(): NumberModel {
        return this[_getPropertyModel]("doubleProp", (parent, key) => new NumberModel(parent, key, false));
    }
    get floatWrapperProp(): NumberModel {
        return this[_getPropertyModel]("floatWrapperProp", (parent, key) => new NumberModel(parent, key, false));
    }
    get floatProp(): NumberModel {
        return this[_getPropertyModel]("floatProp", (parent, key) => new NumberModel(parent, key, false));
    }
    get booleanWrapperProp(): BooleanModel {
        return this[_getPropertyModel]("booleanWrapperProp", (parent, key) => new BooleanModel(parent, key, false));
    }
    get booleanProp(): BooleanModel {
        return this[_getPropertyModel]("booleanProp", (parent, key) => new BooleanModel(parent, key, false));
    }
}
export default FormDataPrimitivesModel;
