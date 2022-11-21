import { _getPropertyModel as _getPropertyModel_1, BooleanModel as BooleanModel_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@hilla/form";
import type FormDataPrimitives_1 from "./FormDataPrimitives";
class FormDataPrimitivesModel<T extends FormDataPrimitives_1 = FormDataPrimitives_1> extends ObjectModel_1<T> {
    declare static createEmptyValue: () => FormDataPrimitives_1;
    get stringProp(): StringModel_1 {
        return this[_getPropertyModel_1]("stringProp", StringModel_1, [true]) as StringModel_1;
    }
    get longWrapperProp(): NumberModel_1 {
        return this[_getPropertyModel_1]("longWrapperProp", NumberModel_1, [true]) as NumberModel_1;
    }
    get longProp(): NumberModel_1 {
        return this[_getPropertyModel_1]("longProp", NumberModel_1, [false]) as NumberModel_1;
    }
    get integerWrapperProp(): NumberModel_1 {
        return this[_getPropertyModel_1]("integerWrapperProp", NumberModel_1, [true]) as NumberModel_1;
    }
    get integerProp(): NumberModel_1 {
        return this[_getPropertyModel_1]("integerProp", NumberModel_1, [false]) as NumberModel_1;
    }
    get doubleWrapperProp(): NumberModel_1 {
        return this[_getPropertyModel_1]("doubleWrapperProp", NumberModel_1, [true]) as NumberModel_1;
    }
    get doubleProp(): NumberModel_1 {
        return this[_getPropertyModel_1]("doubleProp", NumberModel_1, [false]) as NumberModel_1;
    }
    get floatWrapperProp(): NumberModel_1 {
        return this[_getPropertyModel_1]("floatWrapperProp", NumberModel_1, [true]) as NumberModel_1;
    }
    get floatProp(): NumberModel_1 {
        return this[_getPropertyModel_1]("floatProp", NumberModel_1, [false]) as NumberModel_1;
    }
    get booleanWrapperProp(): BooleanModel_1 {
        return this[_getPropertyModel_1]("booleanWrapperProp", BooleanModel_1, [true]) as BooleanModel_1;
    }
    get booleanProp(): BooleanModel_1 {
        return this[_getPropertyModel_1]("booleanProp", BooleanModel_1, [false]) as BooleanModel_1;
    }
}
export default FormDataPrimitivesModel;
