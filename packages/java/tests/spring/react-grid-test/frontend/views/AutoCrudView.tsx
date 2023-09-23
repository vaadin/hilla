import { AutoGrid } from '@hilla/react-grid';
import AppointmentModel from 'Frontend/generated/dev/hilla/test/reactgrid/AppointmentModel';
import { AppointmentService } from 'Frontend/generated/endpoints';

export function AutoCrudView() {
  return <AutoGrid service={AppointmentService} model={AppointmentModel} {...{ pageSize: 10 }} />;
  /* page size is defined only to make testing easier */
}
