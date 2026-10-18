interface Validated {
    id?: number;
    name?: string;
    count: number;
    address?: string;
    code?: string;
    codes?: Array<string | undefined>;
    tags?: Array<string | undefined>;
}
export default Validated;
