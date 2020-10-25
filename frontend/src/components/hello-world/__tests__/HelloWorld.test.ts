import { render } from '@testing-library/react';
import { buildHelloWorld } from './HelloWorldTestBuilder';

describe('<HelloWorld />', () => {
  test('renders learn react link', () => {
    // given
    const what = 'world';
    // when
    const { container } = render(buildHelloWorld({ what }));

    // then
    expect(container).toBeTruthy();
  });
});
