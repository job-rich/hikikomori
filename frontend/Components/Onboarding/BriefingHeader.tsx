import Button from '@/Components/Common/Button/Button';

interface BriefingHeaderProps {
  onSkip: () => void;
}

export default function BriefingHeader({ onSkip }: BriefingHeaderProps) {
  return (
    <header className="onb-briefing">
      <p className="onb-briefing-title">BRIEFING</p>
      <p className="onb-briefing-sub">신규 요원 기밀 브리핑</p>
      <div className="onb-briefing-skip">
        <Button variant="ghost" size="sm" onClick={onSkip}>
          건너뛰기 »
        </Button>
      </div>
    </header>
  );
}
