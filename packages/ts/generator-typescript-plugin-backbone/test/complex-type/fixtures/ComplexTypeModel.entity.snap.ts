export default interface ComplexTypeModel {
    complexList?: Array<Record<string, Array<string | undefined> | undefined> | undefined>;
    complexMap?: Record<string, Array<string | undefined> | undefined>;
}
