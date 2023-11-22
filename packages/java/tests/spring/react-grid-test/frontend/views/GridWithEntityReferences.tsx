import { AutoGrid } from '@hilla/react-crud';
import EmployeeModel from 'Frontend/generated/dev/hilla/test/reactgrid/entityreferences/EmployeeModel.js';
import { EmployeeService } from 'Frontend/generated/endpoints.js';

export function GridWithEntityReferences(): JSX.Element {
  return <div className="p-l">
    <h3 className="mb-s">Default columns</h3>
    <AutoGrid service={EmployeeService} model={EmployeeModel} />

    <h3 className="mt-l mb-s">Customized columns</h3>
    <AutoGrid service={EmployeeService} model={EmployeeModel} visibleColumns={['name', 'homeAddress.streetAddress', 'homeAddress.city', 'department.name']} />
  </div> ;
}
