'use client';

import { useState } from 'react';
import { TAGS, TAG_STYLES } from '@/lib/utils/tagColors';

interface PostFormProps {
  onSubmit: (title: string, content: string, tag: string) => void;
}

export default function PostForm({ onSubmit }: PostFormProps) {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [selectedTag, setSelectedTag] = useState<string>(TAGS[0]);

  const handleSubmit = () => {
    if (!title.trim() || !content.trim()) return;
    onSubmit(title, content, selectedTag);
    setTitle('');
    setContent('');
  };

  return (
    <div className="w-full rounded-lg border border-border bg-card">
      <div className="space-y-3 p-4">
        <input
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="제목을 입력하세요"
          className="w-full border-b border-border bg-transparent pb-2 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none"
        />

        {/* 태그 선택 */}
        <div className="flex flex-wrap gap-2">
          {TAGS.map((tag) => (
            <button
              key={tag}
              type="button"
              onClick={() => setSelectedTag(tag)}
              className={`rounded-full px-3 py-1 text-xs font-medium transition-all ${
                selectedTag === tag
                  ? `${TAG_STYLES[tag]} ring-2 ring-offset-1 ring-offset-background`
                  : `${TAG_STYLES[tag]} opacity-50 hover:opacity-75`
              }`}
            >
              {tag}
            </button>
          ))}
        </div>

        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="당신의 생각을 적어주세요..."
          rows={4}
          className="w-full resize-none bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none"
          onKeyDown={(e) => {
            if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) {
              handleSubmit();
            }
          }}
        />
      </div>

      <div className="flex items-center justify-between border-t border-border px-4 py-2">
        <span className="text-xs text-muted-foreground">
          Ctrl + Enter로 빠르게 게시
        </span>
        <button
          type="button"
          onClick={handleSubmit}
          disabled={!title.trim() || !content.trim()}
          className="rounded-md bg-accent px-4 py-1.5 text-sm font-medium text-accent-foreground transition-colors hover:bg-accent/90 disabled:cursor-not-allowed disabled:opacity-50"
          aria-label="글 게시"
        >
          게시
        </button>
      </div>
    </div>
  );
}
