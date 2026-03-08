import PostDetail from './PostDetail';

interface PageProps {
  params: Promise<{ id: string }>;
}

export default async function PostPage({ params }: PageProps) {
  const { id } = await params;
  return <PostDetail postId={id} />;
}
