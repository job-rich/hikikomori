'use client';

import Navbar from '@/Components/Home/Navbar/Navbar';
import Body from '@/Components/Home/Body/Body';
import Footer from '@/Components/Home/Footer/Footer';
import { RealityGlitch } from '../Common/Overlay/RealityGlitch';
import { FloatingEntity } from '../Common/Overlay/FloatingEntity';
import { ScanlineOverlay } from '../Common/Overlay/ScanLine';
export default function Home() {
  return (
    <>
      <ScanlineOverlay />
      <RealityGlitch />
      <FloatingEntity />
      <Navbar />
      <Body />
      <Footer />
    </>
  );
}
