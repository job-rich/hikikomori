interface Props {
  text: string;
  query: string;
}

export default function SuggestionHighlight({ text, query }: Props) {
  if (!query) return <>{text}</>;

  const idx = text.toLowerCase().indexOf(query.toLowerCase());
  if (idx === -1) return <>{text}</>;

  const before = text.slice(0, idx);
  const match = text.slice(idx, idx + query.length);
  const after = text.slice(idx + query.length);

  return (
    <>
      {before && <b>{before}</b>}
      {match}
      {after && <b>{after}</b>}
    </>
  );
}
