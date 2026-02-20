'use client';

import Navbar from '@/Components/Home/Navbar/Navbar';
import Body from '@/Components/Home/Body/Body';
import Footer from '@/Components/Home/Footer/Footer';
import Side from '@/Components/Home/Side/Side';

// 오버레이
import { TransformOverlay } from '@/Components/Common/Overlay/TransformOverlay';
import { ScanOverlay } from '@/Components/Common/Overlay/ScanOverlay';

export default function Home() {
  return (
    <section>
      {/* 오버레이 임시 비활성화 */}
      <ScanOverlay />
      <TransformOverlay />
      <header>
        <Navbar />
      </header>
      <main className="flex w-full">
        <Body />
        <Side />
      </main>
      <footer>
        <Footer />
      </footer>
    </section>
  );
}
