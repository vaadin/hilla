import type Employee from './Employee.js';
import type ZipCode from './ZipCode.js';

export default interface Person {
  name: string;
  zip: ZipCode;
  team: ReadonlyArray<Employee | undefined>;
}
