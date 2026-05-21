import { useUserStore } from '@/lib/stores/userStore';

interface PostTabProps {
  viewMode: 'all' | 'my';
  setViewMode: (viewMode: 'all' | 'my') => void;
}

export default function PostTab({ viewMode, setViewMode }: PostTabProps) {
  const isLoggedIn = useUserStore(
    (s) => s.nickname !== null && s.snowflakeId !== null
  );
  return (
    <div className="flex w-full border-b border-border">
      <button
        type="button"
        onClick={() => setViewMode('all')}
        className={`px-4 py-2.5 text-sm font-medium transition-colors ${
          viewMode === 'all'
            ? 'border-b-2 border-foreground text-foreground'
            : 'text-muted-foreground hover:text-foreground'
        }`}
      >
        전체 게시글
      </button>
      {isLoggedIn && (
        <button
          type="button"
          onClick={() => setViewMode('my')}
          className={`px-4 py-2.5 text-sm font-medium transition-colors ${
            viewMode === 'my'
              ? 'border-b-2 border-foreground text-foreground'
              : 'text-muted-foreground hover:text-foreground'
          }`}
        >
          내가 쓴 글
        </button>
      )}
    </div>
  );
}
