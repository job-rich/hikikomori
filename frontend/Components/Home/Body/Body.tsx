'use client';

import PostForm from '@/Components/Common/Post/Post-Form/Post-Form';
import PostCard from '@/Components/Common/Post/Post-Card/Post-Card';
export default function Body() {
  const handleSubmit = (content: string, tag: string) => {
    console.log(content, tag);
  };

  return (
    <main className="min-w-0 flex-1 font-sans">
      <div className="mx-auto flex max-w-3xl flex-col items-start">
        <PostForm onSubmit={handleSubmit} />
        <PostCard
          id={1}
          content="test"
          timestamp="test"
          replies={0}
          views={0}
          username="test"
          votes={0}
        />
      </div>
    </main>
  );
}
