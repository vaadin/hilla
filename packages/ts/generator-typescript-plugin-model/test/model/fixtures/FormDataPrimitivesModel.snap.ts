import { _getPropertyModel as _getPropertyModel_1, BooleanModel as BooleanModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@hilla/form";
import type FormDataPrimitives_1 from "./FormDataPrimitives.js";
class FormDataPrimitivesModel<T extends FormDataPrimitives_1 = FormDataPrimitives_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(FormDataPrimitivesModel);
    get stringProp(): StringModel_1 {
        return this[_getPropertyModel_1]("stringProp", (parent, key) => new StringModel_1(parent, key, false));
    }
    get longWrapperProp(): NumberModel_1 {
        return this[_getPropertyModel_1]("longWrapperProp", (parent, key) => new NumberModel_1(parent, key, false));
    }
    get longProp(): NumberModel_1 {
        return this[_getPropertyModel_1]("longProp", (parent, key) => new NumberModel_1(parent, key, false));
    }
    get integerWrapperProp(): NumberModel_1 {
        return this[_getPropertyModel_1]("integerWrapperProp", (parent, key) => new NumberModel_1(parent, key, false));
    }
    get integerProp(): NumberModel_1 {
        return this[_getPropertyModel_1]("integerProp", (parent, key) => new NumberModel_1(parent, key, false));
    }
    get doubleWrapperProp(): NumberModel_1 {
        return this[_getPropertyModel_1]("doubleWrapperProp", (parent, key) => new NumberModel_1(parent, key, false));
    }
    get doubleProp(): NumberModel_1 {
        return this[_getPropertyModel_1]("doubleProp", (parent, key) => new NumberModel_1(parent, key, false));
    }
    get floatWrapperProp(): NumberModel_1 {
        return this[_getPropertyModel_1]("floatWrapperProp", (parent, key) => new NumberModel_1(parent, key, false));
    }
    get floatProp(): NumberModel_1 {
        return this[_getPropertyModel_1]("floatProp", (parent, key) => new NumberModel_1(parent, key, false));
    }
    get booleanWrapperProp(): BooleanModel_1 {
        return this[_getPropertyModel_1]("booleanWrapperProp", (parent, key) => new BooleanModel_1(parent, key, false));
    }
    get booleanProp(): BooleanModel_1 {
        return this[_getPropertyModel_1]("booleanProp", (parent, key) => new BooleanModel_1(parent, key, false));
    }
}
export default FormDataPrimitivesModel;
