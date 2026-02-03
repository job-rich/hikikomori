import { ButtonProps } from '@/lib/types/button';

export default function Button({
  type,
  setCreateOpen,
  setDeleteOpen,
}: ButtonProps) {
  if (type === 'create') {
    return (
      <div>
        <button
          className="border border-green-500 cursor-pointer"
          onClick={() => setCreateOpen(true)}
        >
          Create Button
        </button>
      </div>
    );
  }
  if (type === 'delete') {
    return (
      <div onClick={() => setDeleteOpen(true)}>
        <button className="border border-red-500 cursor-pointer">
          Delete Button
        </button>
      </div>
    );
  }
  if (type === 'update') {
    return (
      <div>
        <button className="border border-yellow-500 cursor-pointer">
          Update Button
        </button>
      </div>
    );
  }
  if (type === 'cancel') {
    return (
      <div>
        <button className="border border-gray-500 cursor-pointer">
          Cancel Button
        </button>
      </div>
    );
  }
}
