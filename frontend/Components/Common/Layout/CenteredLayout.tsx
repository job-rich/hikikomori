import { ReactNode } from 'react';

interface CenteredLayoutProps {
  children: ReactNode;
}

export default function CenteredLayout({ children }: CenteredLayoutProps) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-zinc-50 dark:bg-black">
      {children}
    </div>
  );
}
