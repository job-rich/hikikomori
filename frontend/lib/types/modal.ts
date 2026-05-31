import type { ReactNode } from 'react';

export interface ModalProps {
  open: boolean;
  onClose: () => void;
  children: ReactNode;
  closeOnBackdrop?: boolean;
  closeOnEscape?: boolean;
  ariaLabel?: string;
  className?: string;
  backdropClassName?: string;
}
