type SortTab = 'latest' | 'votes' | 'comments';

interface FillterTabProps {
  sortTab: SortTab;
  setSortTab: (sortTab: SortTab) => void;
}

export default function FillterTab({ sortTab, setSortTab }: FillterTabProps) {
  return (
    <div className="mt-6 flex w-full items-center gap-1 text-sm">
      {(
        [
          { key: 'latest', label: '최신순' },
          { key: 'votes', label: '추천순' },
          { key: 'comments', label: '댓글순' },
        ] as const
      ).map(({ key, label }) => (
        <button
          key={key}
          type="button"
          onClick={() => setSortTab(key)}
          className={`px-3 py-1.5 transition-colors ${
            sortTab === key
              ? 'border-b-2 border-foreground font-semibold text-foreground'
              : 'text-muted-foreground hover:text-foreground'
          }`}
        >
          {label}
        </button>
      ))}
    </div>
  );
}
