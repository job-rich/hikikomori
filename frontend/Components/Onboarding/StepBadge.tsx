interface StepBadgeProps {
  label: string;
}

export default function StepBadge({ label }: StepBadgeProps) {
  return <span className="onb-step-badge">{label}</span>;
}
