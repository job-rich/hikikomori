import type { ReactNode } from 'react';

interface HighlightProps {
  color?: string;
  children: ReactNode;
}

const DEFAULT_COLOR = '#ef4444';

export default function Highlight({
  color = DEFAULT_COLOR,
  children,
}: HighlightProps) {
  return <span style={{ color, fontWeight: 600 }}>{children}</span>;
}
