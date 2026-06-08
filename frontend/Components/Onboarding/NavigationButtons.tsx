import Button from '@/Components/Common/Button/Button';

interface NavigationButtonsProps {
  isFirst: boolean;
  isLast: boolean;
  isNextDisabled?: boolean;
  onPrev: () => void;
  onNext: () => void;
}

export default function NavigationButtons({
  isFirst,
  isLast,
  isNextDisabled = false,
  onPrev,
  onNext,
}: NavigationButtonsProps) {
  return (
    <div className="onb-nav">
      <Button
        variant="secondary"
        size="md"
        onClick={onPrev}
        disabled={isFirst}
        className="flex-[1]"
      >
        « 이전
      </Button>
      <Button
        variant="primary"
        size={isLast ? 'lg' : 'md'}
        onClick={onNext}
        disabled={isNextDisabled}
        className="flex-[2]"
      >
        {isLast ? '입장하기' : '다음 »'}
      </Button>
    </div>
  );
}
