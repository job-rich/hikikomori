const EPOCH = BigInt(1767225600000); // 2026-01-01T00:00:00Z
let sequence = BigInt(0);
let lastTimestamp = BigInt(0);

export function generateSnowflakeId(): string {
  const timestamp = BigInt(Date.now()) - EPOCH;

  if (timestamp === lastTimestamp) {
    sequence = (sequence + BigInt(1)) & BigInt(0xfff);
  } else {
    sequence = BigInt(Math.floor(Math.random() * 64));
    lastTimestamp = timestamp;
  }

  const randomBits = BigInt(Math.floor(Math.random() * 1024));

  const id = (timestamp << BigInt(22)) | (randomBits << BigInt(12)) | sequence;
  return id.toString();
}
