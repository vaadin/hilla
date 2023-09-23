import type { TextFieldElement } from '@hilla/react-components/TextField.js';
import type { RenderResult } from '@testing-library/react';
import { nextFrame } from './grid-test-helpers';

export async function getFormField(result: RenderResult, label: string): Promise<TextFieldElement> {
  return (await result.findByLabelText(label)).parentElement as TextFieldElement;
}

export async function setFormField(result: RenderResult, label: string, value: string): Promise<void> {
  const field = await getFormField(result, label);
  field.value = value;
  field.dispatchEvent(new CustomEvent('input'));
  await nextFrame();
}
export async function submit(result: RenderResult): Promise<void> {
  const submitButton = await result.findByText('Submit');
  submitButton.click();
}
