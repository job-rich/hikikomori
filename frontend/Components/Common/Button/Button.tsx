'use client';

import type {
  ButtonProps,
  ButtonSize,
  ButtonVariant,
} from '@/lib/types/button';

const VARIANT_CLASS: Record<ButtonVariant, string> = {
  primary:
    'border border-[#ef4444] bg-[rgba(239,68,68,0.15)] text-[#ef4444] hover:bg-[rgba(239,68,68,0.25)]',
  secondary:
    'border border-[#3f3f46] bg-transparent text-[#a1a1aa] hover:border-[#52525b]',
  danger:
    'border border-red-500 bg-red-500/10 text-red-500 hover:bg-red-500/20',
  ghost:
    'border border-transparent bg-transparent text-[#71717a] hover:text-[#a1a1aa]',
  success:
    'border border-green-500 bg-green-500/10 text-green-500 hover:bg-green-500/20',
};

const SIZE_CLASS: Record<ButtonSize, string> = {
  sm: 'h-7 px-3 text-[11px] tracking-[1px]',
  md: 'h-10 px-4 text-[13px] tracking-[2px]',
  lg: 'h-12 px-5 text-[14px] tracking-[3px]',
};

const BASE_CLASS =
  'inline-flex items-center justify-center font-mono uppercase transition-colors disabled:cursor-not-allowed disabled:opacity-30 focus:outline-none focus-visible:ring-2 focus-visible:ring-[#ef4444]/40';

export default function Button({
  variant = 'primary',
  size = 'md',
  fullWidth = false,
  className = '',
  type = 'button',
  children,
  ...rest
}: ButtonProps) {
  const widthClass = fullWidth ? 'w-full' : '';
  return (
    <button
      type={type}
      className={`${BASE_CLASS} ${VARIANT_CLASS[variant]} ${SIZE_CLASS[size]} ${widthClass} ${className}`.trim()}
      {...rest}
    >
      {children}
    </button>
  );
}
