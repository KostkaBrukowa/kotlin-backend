import React, { ReactElement } from 'react';
import { HelloWorld, HelloWorldProps } from '../HelloWorld';

export const buildHelloWorld = (props?: Partial<HelloWorldProps>): ReactElement => (
  <HelloWorld what="test what" {...props} />
);
