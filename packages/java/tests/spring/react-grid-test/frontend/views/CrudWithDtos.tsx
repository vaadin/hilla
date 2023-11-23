import { AutoCrud } from '@hilla/react-crud';
import { DepartmentReferenceService, EmployeeDtoService } from 'Frontend/generated/endpoints.js';
import EmployeeDtoModel from 'Frontend/generated/dev/hilla/test/reactgrid/entityreferences/EmployeeDtoModel';
import React, { useEffect, useMemo, useState } from 'react';
import EmployeeDto from 'Frontend/generated/dev/hilla/test/reactgrid/entityreferences/EmployeeDto';
import DepartmentReference from 'Frontend/generated/dev/hilla/test/reactgrid/entityreferences/DepartmentReference';
import { Select } from '@hilla/react-components/Select';

function AddressRenderer({ item: employee }: { item: EmployeeDto }) {
  return (
    <div>
      {employee.address.city} / {employee.address.country}
      <br />
      <span className="text-secondary">{employee.address.street}</span>
    </div>
  );
}

export function CrudWithDtos(): JSX.Element {
  const [departments, setDepartments] = useState<DepartmentReference[]>([]);

  useEffect(() => {
    DepartmentReferenceService.listAll().then(setDepartments);
  }, []);
  const departmentOptions = useMemo(
    () => departments.map((d) => ({ value: d.id.toString(), label: d.name })),
    [departments],
  );

  return (
    <div className="p-l">
      <h2 className="mb-m">Employees</h2>
      <AutoCrud
        service={EmployeeDtoService}
        model={EmployeeDtoModel}
        gridProps={{
          visibleColumns: ['name', 'address', 'department.name'],
          columnOptions: {
            address: {
              renderer: AddressRenderer,
            },
            'department.name': {
              header: 'Department',
            },
          },
        }}
        formProps={{
          visibleFields: ['name', 'address.street', 'address.city', 'address.country', 'department.id'],
          fieldOptions: {
            'department.id': {
              label: 'Department',
              renderer({ field }) {
                return <Select items={departmentOptions} {...field} />;
              },
            },
          },
        }}
      />
    </div>
  );
}
