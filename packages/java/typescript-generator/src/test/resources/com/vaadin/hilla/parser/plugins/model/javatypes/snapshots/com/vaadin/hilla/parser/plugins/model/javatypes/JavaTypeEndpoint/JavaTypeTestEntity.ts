import type CustomEntity_1 from "./CustomEntity.js";
interface JavaTypeTestEntity {
    aBoolean: boolean;
    aNullableBoolean?: boolean;
    aByte: number;
    aNullableByte?: number;
    aChar: string;
    aNullableChar?: string;
    aDouble: number;
    aNullableDouble?: number;
    aFloat: number;
    aNullableFloat?: number;
    aInt: number;
    aNullableInt?: number;
    aLong: number;
    aNullableLong?: number;
    aShort: number;
    aNullableShort?: number;
    aString?: string;
    aDate?: string;
    aLocalDate?: string;
    aLocalTime?: string;
    aLocalDateTime?: string;
    aStringArray?: Array<string | undefined>;
    aByteArray?: Array<number>;
    aStringList?: Array<string | undefined>;
    aCustomEntity?: CustomEntity_1;
}
export default JavaTypeTestEntity;
