import { AutoForm, DeleteErrorEvent, DeleteEvent, SubmitErrorEvent, SubmitEvent } from '@hilla/react-crud';
import { useState } from 'react';
import type Appointment from 'Frontend/generated/dev/hilla/test/reactgrid/Appointment.js';
import AppointmentModel from 'Frontend/generated/dev/hilla/test/reactgrid/AppointmentModel.js';
import { AppointmentService } from 'Frontend/generated/endpoints.js';
import {
  BinderRoot,
  ValidationResult
} from "@hilla/form";

export function AutoFormView(): JSX.Element {
  const [submitted, setSubmitted] = useState<Appointment | undefined>(undefined);
  const [count, setCount] = useState(0);

  function handleSubmit({ item }: SubmitEvent<Appointment>) {
    setSubmitted(item);
  }

  function handleSubmitError({ error }: SubmitErrorEvent) {
    console.error('Error submitting form', error);
  }

  function handleDelete({ item }: DeleteEvent<Appointment>) {
    setSubmitted(undefined);
  }

  function handleDeleteError({ error }: DeleteErrorEvent) {
    console.error('Error deleting appointment', error);
  }

  return (
    <>
      {submitted ? (
        <div id="form-submitted">Thank you {submitted.name}, your appointment has been reserved.</div>
      ) : (
        <>
          <h1>Make a new appointment</h1>
          <button onClick={() => setCount(count + 1)}>Count: {count}</button>
          <br />

          <AutoForm
            service={AppointmentService}
            model={AppointmentModel}
            onSubmitSuccess={handleSubmit}
            onSubmitError={handleSubmitError}
            onDeleteSuccess={handleDelete}
            onDeleteError={handleDeleteError}
            fieldOptions={{doctor: {validators: [
                  {message: 'fail',
                  validate(value: unknown, binder: BinderRoot): boolean {
                    return (!value) || String(value).length < 3;
                  }
                  }
                ]}}}
          />
        </>
      )}
    </>
  );
}
