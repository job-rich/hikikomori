export function formatDate(timestamp: string): string {
  return timestamp.replace('T', ' ').slice(0, 19);
}
