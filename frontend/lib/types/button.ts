export interface ButtonProps {
  type: 'create' | 'delete' | 'update' | 'cancel';
  setCreateOpen: (open: boolean) => void;
  setDeleteOpen: (open: boolean) => void;
}
