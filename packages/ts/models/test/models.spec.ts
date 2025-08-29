import chaiLike from 'chai-like';
import { beforeEach, chai, describe, expect, it } from 'vitest';
import m, {
  $defaultValue,
  $enum,
  $itemModel,
  $key,
  $members,
  $meta,
  $name,
  $optional,
  $owner,
  $constraints,
  ArrayModel,
  BooleanModel,
  type EnumModel,
  Model,
  NumberModel,
  ObjectModel,
  PrimitiveModel,
  StringModel,
  type Target,
  Null,
  NotNull,
  NotBlank,
  NotEmpty,
  Min,
  Max,
  Size,
  type Value,
  type Constraint,
  type ConstraintDescriptor,
} from '../src/index.js';

chai.use(chaiLike);

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
      .object<Person>('Person') // {}
      .property('name', StringModel) // CV: { name: string } EX: { name: StringModel }
      .property('address', AddressModel) // CV: {name: string, address: Address} EX: { name: StringModel, address: AddressModel }
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

    const EmployeeModel = m.object<Employee>('Employee').property('supervisor', m.optional).build();

    expect(EmployeeModel).to.have.property('supervisor').which.is.instanceof(EmployeeModel);
    expect(EmployeeModel.supervisor).to.have.property('supervisor').which.is.instanceof(EmployeeModel);

    // Type assertion to verify that the resulting type is optional.
    const optionalEmployee: Value<typeof EmployeeModel.supervisor> = undefined;
    expect(optionalEmployee).to.be.undefined;
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
    const target: Target<Named> = { value: { name: 'John Doe' } };
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

    const NamedWithMetaModel = m.meta(
      m.object<NamedWithMeta>('NamedWithMeta').property('name', StringModel).build(),
      meta,
    );

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

  describe('m.value', () => {
    let target: Target<Person>;

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
      const personValue = m.value(PersonModel);
      expect(personValue).to.be.like({
        name: '',
        address: {
          street: {
            name: '',
          },
        },
      });

      expect(m.value(PersonModel.name)).to.be.string('');
      expect(m.value(PersonModel.address)).to.be.like({
        street: {
          name: '',
        },
      });

      expect(m.value(PersonModel.address.street)).to.be.like({
        name: '',
      });

      expect(m.value(PersonModel.address.street.name)).to.be.string('');
    });

    it('returns the value from the attached target', () => {
      const AttachedPersonModel = m.attach(PersonModel, target);

      expect(AttachedPersonModel).to.have.property($owner).which.is.equal(target);
      expect(m.value(AttachedPersonModel)).to.be.equal(target.value);
      expect(m.value(AttachedPersonModel.name)).to.be.equal(target.value.name);
      expect(m.value(AttachedPersonModel.address)).to.be.equal(target.value.address);
      expect(m.value(AttachedPersonModel.address.street)).to.be.equal(target.value.address.street);
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

      it('should allow to iterate through the item models', () => {
        const target: Target<Comment[]> = {
          value: [
            { title: 'FooTitle', text: 'FooText' },
            { title: 'BarTitle', text: 'BarText' },
          ],
        };

        const AttachedCommentsModel = m.attach(m.array(CommentModel), target);

        const items = [...m.items(AttachedCommentsModel)];

        expect(items).to.be.an('array').with.lengthOf(2);

        for (let i = 0; i < items.length; i++) {
          expect(items[i]).to.be.instanceof(CommentModel);
          expect(items[i]).to.have.property($owner).which.is.equal(AttachedCommentsModel);
          expect(items[i]).to.have.property($key).which.is.equal(i);
          expect(m.value(items[i])).to.be.equal(target.value[i]);
        }
      });
    });

    describe('ObjectModel', () => {
      it('should have a default value', () => {
        expect(ObjectModel[$defaultValue]).to.be.like({});
      });
    });
  });

  describe('Validation support', () => {
    it('should support defining validators on properties', () => {
      const Titled = ((_model: unknown) => undefined) as unknown as Constraint<{
        readonly title: string;
        readonly titledMarker?: string;
      }>;

      // Verify the type of the constraints
      m.constrained(StringModel, NotNull());
      m.constrained(NumberModel, NotNull());
      m.constrained(BooleanModel, NotNull());
      m.constrained(m.array(StringModel), NotNull());
      m.constrained(m.optional(NamedModel), NotNull());
      m.constrained(m.optional(NamedModel), NotNull({ message: 'message' }));

      m.constrained(m.array(StringModel), Null());
      m.constrained(m.optional(NamedModel), Null({ message: 'message' }));

      m.constrained(m.array(NumberModel), NotEmpty());
      m.constrained(StringModel, NotEmpty());
      m.constrained(NumberModel, Min(1));
      m.constrained(NumberModel, Max(10));

      const commentModelWithConstraints = m
        .object<Comment>('Comment')
        .property('title', StringModel)
        .property('text', m.constrained(StringModel, NotBlank(), NotEmpty(), Size({ max: 140 })))
        .build();

      // Verify the type of the constrained model
      CommentModel = commentModelWithConstraints;

      m.constrained(commentModelWithConstraints.text, NotBlank());
      m.constrained(m.optional(CommentModel.text), NotBlank());
      m.constrained(commentModelWithConstraints.text, NotEmpty());
      m.constrained(commentModelWithConstraints.text, NotEmpty(), NotBlank());

      m.constrained(CommentModel, Titled);

      m.constrained(NumberModel, Min(1));

      expect(CommentModel[$constraints]).to.be.undefined;
      expect(CommentModel.text[$constraints]).to.be.an('array').with.lengthOf(1);
      const [, descriptor] = CommentModel.text[$constraints]!;
      expect(descriptor.declaration).to.equal(Size);
      if (descriptor.declaration === Size) {
        const {
          attributes: { min, max },
        } = descriptor as ConstraintDescriptor<typeof Size>;
        expect(min).to.be.equal(1);
        expect(max).to.be.equal(140);
      }
      expect(descriptor.attributes).to.be.like({});
      expect(descriptor.declaration).to.equal(NotEmpty);
    });

    it('should support defining validators on the model', () => {
      PersonModel = m
        .extend(PersonModel)
        .object<Person>('Person')
        .property('comments', m.constrained(m.array(CommentModel), NotEmpty()))
        .build();

      expect(PersonModel[$constraints]).to.be.undefined;
      expect(PersonModel.comments).to.be.an('array').with.lengthOf(1);
      expect(PersonModel.comments[$constraints]![0].attributes).to.be.like({});
      expect(PersonModel.comments[$constraints]![0].declaration).to.equal(NotEmpty);
      expect(PersonModel.comments[$itemModel].text).to.be.undefined;
    });
  });
});
