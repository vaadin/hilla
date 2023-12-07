import { AutoGrid } from '@hilla/react-crud';
import type Employee from 'Frontend/generated/dev/hilla/test/reactgrid/entityreferences/Employee.js';
import EmployeeModel from 'Frontend/generated/dev/hilla/test/reactgrid/entityreferences/EmployeeModel.js';
import { EmployeeService } from 'Frontend/generated/endpoints.js';

function DepartmentRenderer({item}: {item: Employee}) {
  return <>{item.department.name}</>;
}

export function GridWithEntityReferences(): JSX.Element {
  return <AutoGrid pageSize={10} service={EmployeeService} model={EmployeeModel} noHeaderFilters
   columnOptions={{
     "department.name": {
       renderer: DepartmentRenderer,
       header: "Department"
     },
   }}/>;
  /* page size is defined only to make testing easier */
}
