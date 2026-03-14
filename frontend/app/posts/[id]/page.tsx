import Navbar from '@/Components/Home/Navbar/Navbar';
import Footer from '@/Components/Home/Footer/Footer';
import PostDetail from '@/Components/Common/Post/Post-Detail/Post-Detail';

interface PostPageProps {
  params: Promise<{ id: string }>;
}

export default async function PostPage({ params }: PostPageProps) {
  const { id } = await params;

  return (
    <div className="flex min-h-screen flex-col">
      <Navbar />
      <div className="flex-1">
        <PostDetail postId={id} />
      </div>
      <Footer />
    </div>
  );
}
