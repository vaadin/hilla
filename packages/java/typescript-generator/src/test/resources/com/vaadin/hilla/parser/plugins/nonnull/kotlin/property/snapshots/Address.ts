import type AbstractEntity from './AbstractEntity.js';

interface Address extends AbstractEntity {
  id?: number;
  street: string;
  zipCode: string;
  city?: string;
  label?: string;
}

export default Address;
