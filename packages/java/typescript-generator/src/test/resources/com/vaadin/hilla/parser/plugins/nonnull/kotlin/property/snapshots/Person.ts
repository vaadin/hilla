import type AbstractEntity from './AbstractEntity.js';
import type Address from './Address.js';

interface Person extends AbstractEntity {
  id?: number;
  firstName: string;
  lastName: string;
  email?: string;
  phone: string;
  important: boolean;
  age?: number;
  luckyNumber: number;
  addresses: Record<string, Address>;
  profilePicture?: string;
  alias: string;
  badge: string;
  fullName: string;
}

export default Person;
