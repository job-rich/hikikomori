import { TOTAL_STEPS } from '@/lib/data/onboarding';

interface StepIndicatorProps {
  current: number;
}

export default function StepIndicator({ current }: StepIndicatorProps) {
  return (
    <div className="onb-indicator" role="tablist" aria-label="온보딩 진행 상태">
      {Array.from({ length: TOTAL_STEPS }, (_, idx) => {
        const dotClass =
          idx === current
            ? 'onb-dot onb-dot--active'
            : idx < current
              ? 'onb-dot onb-dot--passed'
              : 'onb-dot';
        return (
          <span
            key={idx}
            className={dotClass}
            role="tab"
            aria-selected={idx === current}
            aria-label={`${idx + 1}단계`}
          />
        );
      })}
    </div>
  );
}
