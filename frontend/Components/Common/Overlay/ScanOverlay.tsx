'use client';

export function ScanOverlay() {
  return (
    <div
      className="pointer-events-none fixed inset-0 z-[998] overflow-hidden"
      aria-hidden="true"
    >
      {/* bg-neon/30에서 색상 변경 가능 */}
      <div className="animate-scanline-thick absolute left-0 w-full h-[2px] bg-neon/30 blur-[1px]" />
    </div>
  );
}
