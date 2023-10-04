import { ExperimentalAutoForm } from '@hilla/react-grid';
import Appointment from 'Frontend/generated/dev/hilla/test/reactgrid/Appointment';
import AppointmentModel from 'Frontend/generated/dev/hilla/test/reactgrid/AppointmentModel';
import { AppointmentService } from 'Frontend/generated/endpoints';
import { useState } from 'react';

export function AutoFormView() {
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
