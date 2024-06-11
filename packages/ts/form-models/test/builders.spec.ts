import { CoreModelBuilder, ObjectModelBuilder } from '../src/builders.js';
import { Model } from '../src/model.js';

describe('ModelBuilder', () => {
  it('should create a model', () => {
    const StringModel = new CoreModelBuilder('StringModel', Model, () => '').build();
  });

  it('should create a model with properties', () => {
    type Employee = Readonly<{
      name: string;
      age: number;
    }>;

    const NumberModel = new CoreModelBuilder('NumberModel', Model, () => 0).build();
    const StringModel = new CoreModelBuilder('StringModel', Model, () => '').build();

    const EmployeeModel = ObjectModelBuilder.from<Employee>(Model)
      .name('EmployeeModel')
      .property('name', StringModel)
      .property('age', NumberModel)
      .build();
  });
});
