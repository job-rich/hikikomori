'use client';

import { useEffect } from 'react';
import type { ModalProps } from '@/lib/types/modal';

export default function Modal({
  open,
  onClose,
  children,
  closeOnBackdrop = true,
  closeOnEscape = true,
  ariaLabel,
  className = '',
  backdropClassName = 'bg-black/70',
}: ModalProps) {
  useEffect(() => {
    if (!open) return;
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = previousOverflow;
    };
  }, [open]);

  useEffect(() => {
    if (!open || !closeOnEscape) return;
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [open, closeOnEscape, onClose]);

  if (!open) return null;

  return (
    <div
      className={`modal-backdrop modal-enter ${backdropClassName}`}
      role="dialog"
      aria-modal="true"
      aria-label={ariaLabel}
      onClick={(event) => {
        if (closeOnBackdrop && event.target === event.currentTarget) onClose();
      }}
    >
      <div className={`modal-content modal-content-enter ${className}`}>
        {children}
      </div>
    </div>
  );
}
