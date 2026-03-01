'use client';

import { useState } from 'react';
import { Send } from 'lucide-react';

interface PostFormProps {
  onSubmit: (title: string, content: string, tag: string) => void;
}

export default function PostForm({ onSubmit }: PostFormProps) {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [selectedTag, setSelectedTag] = useState('VOID');

  const handleSubmit = () => {
    if (!title.trim() || !content.trim()) return;
    onSubmit(title, content, selectedTag);
    setTitle('');
    setContent('');
  };

  return (
    <div className="border border-border rounded-md bg-card w-full">
      <div className="p-4 space-y-3">
        <input
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="제목을 입력하세요..."
          className="w-full bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none border-b border-border pb-2"
        />
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="여기에 글을 작성하세요..."
          rows={4}
          className="w-full resize-none bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none"
          onKeyDown={(e) => {
            if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) {
              handleSubmit();
            }
          }}
        />
      </div>

      <div className="flex justify-end gap-2 px-4 py-2 border-t border-border">
        <button
          type="button"
          onClick={handleSubmit}
          disabled={!title.trim() || !content.trim()}
          className="flex items-center gap-1.5 px-3 py-1.5 text-sm border border-border rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-muted"
          aria-label="글 게시"
        >
          <Send className="h-4 w-4" />
          게시
        </button>
      </div>
    </div>
  );
}
