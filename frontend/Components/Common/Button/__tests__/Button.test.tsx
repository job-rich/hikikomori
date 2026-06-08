import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import Button from '@/Components/Common/Button/Button';

afterEach(cleanup);

describe('Button', () => {
  it('children 을 렌더한다', () => {
    render(<Button>입장</Button>);
    expect(screen.getByRole('button', { name: '입장' })).toBeInTheDocument();
  });

  it('variant 별로 다른 className 을 적용한다', () => {
    const { rerender } = render(<Button variant="primary">P</Button>);
    expect(screen.getByRole('button', { name: 'P' }).className).toMatch(
      /text-\[#ef4444\]/
    );

    rerender(<Button variant="secondary">S</Button>);
    expect(screen.getByRole('button', { name: 'S' }).className).toMatch(
      /text-\[#a1a1aa\]/
    );

    rerender(<Button variant="success">G</Button>);
    expect(screen.getByRole('button', { name: 'G' }).className).toMatch(
      /text-green-500/
    );
  });

  it('onClick 을 호출한다', () => {
    const onClick = vi.fn();
    render(<Button onClick={onClick}>탭</Button>);
    fireEvent.click(screen.getByRole('button', { name: '탭' }));
    expect(onClick).toHaveBeenCalledOnce();
  });

  it('disabled 일 때 클릭이 막힌다', () => {
    const onClick = vi.fn();
    render(
      <Button disabled onClick={onClick}>
        탭
      </Button>
    );
    fireEvent.click(screen.getByRole('button', { name: '탭' }));
    expect(onClick).not.toHaveBeenCalled();
  });

  it('fullWidth 시 w-full 을 포함한다', () => {
    render(<Button fullWidth>W</Button>);
    expect(screen.getByRole('button', { name: 'W' }).className).toMatch(
      /w-full/
    );
  });

  it('기본 type=button 으로 form submit 을 막는다', () => {
    render(<Button>B</Button>);
    expect(screen.getByRole('button', { name: 'B' })).toHaveAttribute(
      'type',
      'button'
    );
  });
});
