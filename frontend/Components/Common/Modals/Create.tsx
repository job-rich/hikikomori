export default function Create({
  setCreateOpen,
}: {
  setCreateOpen: (open: boolean) => void;
}) {
  return (
    <>
      <style
        dangerouslySetInnerHTML={{
          __html: `
            @keyframes modalFadeIn { from { opacity: 0; } to { opacity: 1; } }
            @keyframes modalFadeInScale { from { opacity: 0; transform: scale(0.96); } to { opacity: 1; transform: scale(1); } }
          `,
        }}
      />
      <div className="fixed inset-0 z-10 flex items-center justify-center bg-black/50 animate-[modalFadeIn_0.2s_ease-out]">
        <div
          className="border border-green-500 w-[300px] h-[300px] flex items-center justify-center bg-green-500 animate-[modalFadeInScale_0.25s_ease-out]"
          onClick={() => setCreateOpen(false)}
        >
          글 추가
        </div>
      </div>
    </>
  );
}
