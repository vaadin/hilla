import { ExperimentalAutoForm } from '@hilla/react-grid';
import { useState } from 'react';
import type Appointment from 'Frontend/generated/dev/hilla/test/reactgrid/Appointment';
import AppointmentModel from 'Frontend/generated/dev/hilla/test/reactgrid/AppointmentModel';
import { AppointmentService } from 'Frontend/generated/endpoints';

export function AutoFormView(): JSX.Element {
  const [submitted, setSubmitted] = useState<Appointment | undefined>(undefined);
  return (
    <>
      {submitted ? (
        <div id="form-submitted">Thank you {submitted.name}, your appointment has been reserved.</div>
      ) : (
        <>
          <h1>Make a new appointment</h1>
          <br />
          <ExperimentalAutoForm
            service={AppointmentService}
            model={AppointmentModel}
            afterSubmit={({ item }) => {
              setSubmitted(item);
            }}
          />
        </>
      )}
    </>
  );
}
