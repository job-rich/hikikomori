import Highlight from '@/Components/Common/Highlight/Highlight';

interface HighlightMatchProps {
  text: string;
  query: string;
  color?: string;
}

function escapeRegex(str: string): string {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

export default function HighlightMatch({
  text,
  query,
  color,
}: HighlightMatchProps) {
  if (!text) return <>{text}</>;

  const tokens = query.split(/\s+/).filter(Boolean).map(escapeRegex);

  if (!tokens.length) return <>{text}</>;

  const pattern = new RegExp(`(${tokens.join('|')})`, 'gi');
  const parts = text.split(pattern);

  if (parts.length === 1) return <>{text}</>;

  return (
    <>
      {parts.map((part, i) =>
        i % 2 === 1 ? (
          <Highlight key={i} color={color}>
            {part}
          </Highlight>
        ) : (
          part
        )
      )}
    </>
  );
}
