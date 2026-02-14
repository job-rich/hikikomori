import Navbar from '@/Components/Home/Navbar/Navbar';
import Body from '@/Components/Home/Body/Body';
import Footer from '@/Components/Home/Footer/Footer';
import Side from '@/Components/Home/Side/Side';

export default function Home() {
  return (
    <div className="flex min-h-screen flex-col">
      <Navbar />
      <div className="mx-auto mt-10 flex w-full max-w-6xl flex-1 flex-col gap-8 px-4 py-6 sm:flex-row sm:px-6">
        <Body />
        <Side />
      </div>
      <Footer />
    </div>
  );
}
