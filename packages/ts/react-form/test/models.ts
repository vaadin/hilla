import m, {
  type ArrayModel,
  BooleanModel,
  type Model,
  NotEmpty,
  NumberModel,
  type ObjectModel,
  type OptionalModel,
  Size,
  StringModel,
} from '@vaadin/hilla-models';

export interface FormUser {
  name: string;
  password: string;
  passwordHint?: string;
}

export const FormUserModel: ObjectModel<
  FormUser,
  {
    readonly name: Model<string>;
    readonly password: Model<string>;
    readonly passwordHint: OptionalModel<Model<string>>;
  }
> = m
  .object<FormUser>('FormUser')
  .property('name', m.constrained(StringModel, NotEmpty(), Size({ max: 10 })))
  .property('password', m.constrained(StringModel, NotEmpty(), Size({ min: 6 })))
  .property('passwordHint', m.constrained(m.optional(StringModel), Size({ max: 10 })))
  .build();
export type FormUserModel = typeof FormUserModel;

export interface Login {
  user: FormUser;
  rememberMe?: boolean;
}

export const LoginModel: ObjectModel<
  Login,
  { readonly user: FormUserModel; readonly rememberMe: OptionalModel<BooleanModel> }
> = m.object<Login>('Login').property('user', FormUserModel).property('rememberMe', m.optional(BooleanModel)).build();
export type LoginModel = typeof LoginModel;

export interface Entity {
  projectId?: number;
  contractId?: number;
}

const longModel: NumberModel = m.meta(NumberModel, { jvmType: 'java.lang.Long' });

export const EntityModel: ObjectModel<
  Entity,
  { readonly projectId: OptionalModel<NumberModel>; readonly contractId: OptionalModel<NumberModel> }
> = m
  .object<Entity>('Entity')
  .property('projectId', m.optional(longModel))
  .property('contractId', m.optional(longModel))
  .build();
export type EntityModel = typeof EntityModel;

export interface Project {
  id: number;
  name: string;
}

export interface Contract {
  id: number;
  name: string;
}

// Player and Team entities used to test array models
export interface Player {
  id: number;
  firstName: string;
  lastName: string;
  age: number;
}

export interface Team {
  id: number;
  name: string;
  players: Player[];
}

const intModel: NumberModel = m.meta(NumberModel, { jvmType: 'int' });
const stringModel: StringModel = m.meta(StringModel, { jvmType: 'java.lang.String' });

export const PlayerModel: ObjectModel<
  Player,
  {
    readonly id: NumberModel;
    readonly firstName: StringModel;
    readonly lastName: StringModel;
    readonly age: NumberModel;
  }
> = m
  .object<Player>('Player')
  .property('id', intModel)
  .property('firstName', stringModel)
  .property('lastName', stringModel)
  .property('age', intModel)
  .build();
export type PlayerModel = typeof PlayerModel;

export const TeamModel: ObjectModel<
  Team,
  { readonly id: NumberModel; readonly name: StringModel; readonly players: ArrayModel<PlayerModel> }
> = m
  .object<Team>('Team')
  .property('id', intModel)
  .property('name', stringModel)
  .property('players', m.meta(m.array(PlayerModel), { jvmType: 'java.util.List' }))
  .build();
export type TeamModel = typeof TeamModel;

export interface StreetAddress {
  number?: string;
  name?: string;
}

export interface PostalAddress {
  streetAddress?: StreetAddress;
}

export const StreetAddressModel: ObjectModel<
  StreetAddress,
  { readonly number: OptionalModel<StringModel>; readonly name: OptionalModel<StringModel> }
> = m
  .object<StreetAddress>('StreetAddress')
  .property('number', m.optional(stringModel))
  .property('name', m.optional(stringModel))
  .build();
export type StreetAddressModel = typeof StreetAddressModel;

export const PostalAddressModel: ObjectModel<
  PostalAddress,
  { readonly streetAddress: OptionalModel<StreetAddressModel> }
> = m.object<PostalAddress>('PostalAddress').property('streetAddress', m.optional(StreetAddressModel)).build();
export type PostalAddressModel = typeof PostalAddressModel;
