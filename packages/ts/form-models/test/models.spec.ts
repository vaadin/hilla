import { expect, use } from '@esm-bundle/chai';
import chaiLike from 'chai-like';
import m, {
  $defaultValue,
  $enum,
  $itemModel,
  $members,
  $meta,
  $name,
  $optional,
  $owner,
  ArrayModel,
  type AttachTarget,
  BooleanModel,
  type EnumModel,
  getValue,
  Model,
  NumberModel,
  ObjectModel,
  PrimitiveModel,
  StringModel,
} from '../src/index.js';

use(chaiLike);

describe('@vaadin/hilla-form-models', () => {
  enum Role {
    Guest = 'guest',
    User = 'user',
    Admin = 'admin',
  }

  interface Named {
    name: string;
  }

  interface Street extends Named {
    building: number;
  }

  interface Address {
    street: Street;
  }

  interface Comment {
    title: string;
    text: string;
  }

  interface Person {
    name: string;
    address: Address;
    comments: Comment[];
    role: Role;
  }

  let NamedModel: ObjectModel<Named, Readonly<{ name: StringModel }>>;
  let StreetModel: ObjectModel<Street, Readonly<{ building: NumberModel }> & typeof NamedModel>;
  let AddressModel: ObjectModel<Address, Readonly<{ street: typeof StreetModel }>>;
  let CommentModel: ObjectModel<Comment, Readonly<{ title: StringModel; text: StringModel }>>;
  let RoleModel: EnumModel<typeof Role>;
  let PersonModel: ObjectModel<
    Person,
    Readonly<{
      name: StringModel;
      address: typeof AddressModel;
      comments: ArrayModel<typeof CommentModel>;
      role: typeof RoleModel;
    }>
  >;

  beforeEach(() => {
    RoleModel = m.enum(Role, 'Role');
    NamedModel = m.object<Named>('Named').property('name', StringModel).build();
    StreetModel = m.extend(NamedModel).object<Street>('Street').property('building', NumberModel).build();
    AddressModel = m.object<Address>('Address').property('street', StreetModel).build();
    CommentModel = m.object<Comment>('Comment').property('title', StringModel).property('text', StringModel).build();
    PersonModel = m
      .object<Person>('Person')
      .property('name', StringModel)
      .property('address', AddressModel)
      .property('comments', m.array(CommentModel))
      .property('role', RoleModel)
      .build();
  });

  it('should create a simple model', () => {
    expect(NamedModel).to.exist;
    expect(NamedModel).to.be.instanceof(Model);
    expect(NamedModel).to.have.property($name, 'Named');
    expect(NamedModel).to.have.property('name').which.is.instanceof(StringModel);
    expect(NamedModel.toString()).to.be.equal('[:detached: / model] Named');
  });

  it('should allow model inheritance', () => {
    expect(StreetModel).to.be.instanceof(NamedModel);
    expect(StreetModel).to.have.property($name, 'Street');
    // Inherited property
    expect(StreetModel).to.have.property('name').which.is.instanceof(StringModel);
    // Own property
    expect(StreetModel).to.have.property('building').which.is.instanceof(NumberModel);
  });

  it('should allow model composition', () => {
    expect(PersonModel).to.have.property('address').which.is.instanceof(AddressModel);
    expect(AddressModel).to.have.property('street').which.is.instanceof(StreetModel);
    expect(PersonModel.address).to.have.property('street').which.is.instanceof(StreetModel);
    expect(PersonModel.address.street.toString()).to.be.equal(
      '[[[:detached: / model] Person / address] Address / street] Street',
    );
  });

  it('should allow self-reference', () => {
    interface Employee {
      supervisor?: Employee;
    }

    const EmployeeModel = m
      .object<Employee>('Employee')
      // eslint-disable-next-line @typescript-eslint/unbound-method
      .property('supervisor', m.optional)
      .build();

    expect(EmployeeModel).to.have.property('supervisor').which.is.instanceof(EmployeeModel);
    expect(EmployeeModel.supervisor).to.have.property('supervisor').which.is.instanceof(EmployeeModel);
  });

  it('should allow array types', () => {
    expect(PersonModel).to.have.property('comments').which.is.instanceof(ArrayModel);
    expect(PersonModel.comments).to.have.property($itemModel).which.is.instanceof(CommentModel);
  });

  it('should allow enum types', () => {
    expect(RoleModel).to.have.property($name, 'Role');
    expect(RoleModel).to.have.property($enum, Role);
    expect(RoleModel).to.have.property($defaultValue).which.is.equal(Role.Guest);
  });

  it('correctly sets the model owner', () => {
    expect(PersonModel.address.street).to.have.property($owner).which.has.property($owner).which.is.equal(PersonModel);
  });

  it('should allow union types', () => {
    interface User {
      name: string;
    }

    interface Group {
      quantity: number;
    }

    const UserModel = m.object<User>('User').property('name', StringModel).build();
    const GroupModel = m.object<Group>('Group').property('quantity', NumberModel).build();

    const UnionModel = m.union(UserModel, GroupModel);

    expect(UnionModel).to.have.property($members).which.is.an('array').with.lengthOf(2);
    expect(UnionModel).to.have.property($defaultValue).which.is.like({ name: '' });
  });

  it('should has name prefixed with "@" if attached', () => {
    const target: AttachTarget<Named> = { value: { name: 'John Doe' } };
    const AttachedNamedModel = m.attach(NamedModel, target);

    expect(AttachedNamedModel).to.have.property($name, '@Named');
  });

  it('should correctly detect instances', () => {
    expect(PersonModel).to.be.instanceof(Model);
    expect(PersonModel).to.be.instanceof(ObjectModel);
    expect(PersonModel.comments).to.be.instanceof(ArrayModel);
  });

  it('should correctly set the model owner', () => {
    expect(PersonModel.address.street).to.have.property($owner).which.has.property($owner).which.is.equal(PersonModel);
  });

  it('should print the name of the model', () => {
    expect(Object.prototype.toString.call(PersonModel)).to.be.equal('[object Person]');
  });

  it('should append metadata to the model', () => {
    const metadata = { jvmType: 'com.example.Person' };
    const model = m.extend(PersonModel).object<Person>('PersonMeta').meta(metadata).build();

    expect(model).to.have.property($meta).which.is.equal(metadata);
  });

  it('should allow adding a metadata to a property', () => {
    const meta = { jvmType: 'com.example.CustomString' };
    interface NamedWithMeta {
      name: string;
    }

    const NamedWithMetaModel = m.object<NamedWithMeta>('NamedWithMeta').property('name', StringModel, { meta }).build();

    expect(NamedWithMetaModel.name).to.have.property($meta).which.is.equal(meta);
  });

  it('should make an optional model', () => {
    interface Optional {
      name?: string;
    }

    const OptionalModel = m.object<Optional>('Optional').property('name', m.optional(StringModel)).build();

    expect(OptionalModel).to.have.property('name').which.is.instanceof(StringModel);
    expect(OptionalModel.name).to.have.property($optional).which.is.true;
    expect(OptionalModel.name.toString()).to.be.equal('[[:detached: / model] Optional / name?] string');
  });

  describe('getValue', () => {
    let target: AttachTarget<Person>;

    beforeEach(() => {
      target = Object.create(
        { toString: () => 'Target' },
        {
          value: {
            value: {
              name: 'John Doe',
              address: {
                street: {
                  name: 'Main Street',
                },
              },
            },
          },
        },
      );
    });

    it('gets the default value if the model is detached', () => {
      const personValue = getValue(PersonModel);
      expect(personValue).to.be.like({
        name: '',
        address: {
          street: {
            name: '',
          },
        },
      });

      expect(getValue(PersonModel.name)).to.be.string('');
      expect(getValue(PersonModel.address)).to.be.like({
        street: {
          name: '',
        },
      });

      expect(getValue(PersonModel.address.street)).to.be.like({
        name: '',
      });

      expect(getValue(PersonModel.address.street.name)).to.be.string('');
    });

    it('returns the value from the attach target', () => {
      const AttachedPersonModel = m.attach(PersonModel, target);

      expect(AttachedPersonModel).to.have.property($owner).which.is.equal(target);
      expect(getValue(AttachedPersonModel)).to.be.equal(target.value);
      expect(getValue(AttachedPersonModel.name)).to.be.equal(target.value.name);
      expect(getValue(AttachedPersonModel.address)).to.be.equal(target.value.address);
      expect(getValue(AttachedPersonModel.address.street)).to.be.equal(target.value.address.street);
    });
  });

  describe('Core Models', () => {
    describe('PrimitiveModel', () => {
      it('should have the default value', () => {
        expect(PrimitiveModel[$defaultValue]).to.be.equal(undefined);
      });
    });

    describe('StringModel', () => {
      it('should have the default value', () => {
        expect(StringModel[$defaultValue]).to.be.equal('');
      });
    });

    describe('NumberModel', () => {
      it('should have a default value', () => {
        expect(NumberModel[$defaultValue]).to.be.equal(0);
      });
    });

    describe('BooleanModel', () => {
      it('should have a default value', () => {
        expect(BooleanModel[$defaultValue]).to.be.equal(false);
      });
    });

    describe('ArrayModel', () => {
      it('should have a default value', () => {
        expect(ArrayModel[$defaultValue]).to.be.like([]);
      });
    });

    describe('ObjectModel', () => {
      it('should have a default value', () => {
        expect(ObjectModel[$defaultValue]).to.be.like({});
      });
    });
  });
});
