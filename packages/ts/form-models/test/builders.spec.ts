import { expect, use } from '@esm-bundle/chai';
import chaiLike from 'chai-like';
import m, { $defaultValue, $enum, $itemModel, $name, $owner, ArrayModel, Model, StringModel } from '../src/index.js';

use(chaiLike);

describe('ModelBuilder', () => {
  it('should create a simple model', () => {
    interface Named {
      name: string;
    }

    const NamedModel = m.object<Named>('Named').property('name', StringModel).build();

    expect(NamedModel).to.exist;
    expect(NamedModel).to.be.instanceof(Model);
    expect(NamedModel).to.have.property($name, 'Named');
    expect(NamedModel).to.have.property('name');
    expect(NamedModel).to.have.property('name').which.is.instanceof(StringModel);
  });

  it('should allow model inheritance', () => {
    interface Named {
      name: string;
    }

    interface Address extends Named {
      street: string;
    }

    const NamedModel = m.object<Named>('Named').property('name', StringModel).build();

    const AddressModel = m.extend(NamedModel).name<Address>('Address').property('street', StringModel).build();

    expect(AddressModel).to.be.instanceof(NamedModel);
    expect(AddressModel).to.have.property($name, 'Address');
    // Inherited property
    expect(AddressModel).to.have.property('name').which.is.instanceof(StringModel);
    // Own property
    expect(AddressModel).to.have.property('street').which.is.instanceof(StringModel);
  });

  it('should allow model composition', () => {
    interface Person {
      name: string;
    }

    interface Post {
      author: Person;
    }

    const PersonModel = m.object<Person>('Person').property('name', StringModel).build();

    const PostModel = m.object<Post>('Post').property('author', PersonModel).build();

    expect(PostModel).to.have.property('author').which.is.instanceof(PersonModel);
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
    interface Comment {
      text: string;
    }

    interface Post {
      comments: Comment[];
    }

    const CommentModel = m.object<Comment>('Comment').property('text', StringModel).build();

    const PostModel = m.object<Post>('Post').property('comments', m.array(CommentModel)).build();

    expect(PostModel).to.have.property('comments').which.is.instanceof(ArrayModel);
    expect(PostModel.comments).to.have.property($itemModel).which.is.instanceof(CommentModel);
  });

  it('should allow enum types', () => {
    enum Status {
      Pending = 'pending',
      Approved = 'approved',
      Rejected = 'rejected',
    }

    const StatusModel = m.enum(Status, 'Status');

    expect(StatusModel).to.have.property($name, 'Status');
    expect(StatusModel).to.have.property($enum, Status);
    expect(StatusModel).to.have.property($defaultValue).which.is.equal(Status.Pending);
  });

  it('correctly sets the model owner', () => {
    interface Street {
      name: string;
    }

    interface Address {
      street: Street;
    }

    interface Person {
      name: string;
      address: Address;
    }

    const StreetModel = m.object<Street>('Street').property('name', StringModel).build();
    const AddressModel = m.object<Address>('Address').property('street', StreetModel).build();
    const PersonModel = m
      .object<Person>('Person')
      .property('name', StringModel)
      .property('address', AddressModel)
      .build();

    expect(PersonModel.address.street).to.have.property($owner).which.has.property($owner).which.is.equal(PersonModel);
  });

  it('');
});
