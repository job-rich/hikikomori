const BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export class ApiError extends Error {
  constructor(
    public status: number,
    message?: string
  ) {
    super(message ?? `API 요청 실패: ${status}`);
    this.name = 'ApiError';
  }
}

/** err가 ApiError이고, status를 주면 그 상태코드까지 일치하는지 확인한다. */
export function isApiError(err: unknown, status?: number): err is ApiError {
  return err instanceof ApiError && (status === undefined || err.status === status);
}

export async function apiClient<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${BASE_URL}${endpoint}`;
  let response: Response;
  try {
    response = await fetch(url, {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    });
  } catch (err) {
    const hint =
      '백엔드가 실행 중인지(예: ./backend/gradlew -p backend bootRun), ' +
      'NEXT_PUBLIC_API_URL이 맞는지 확인하세요. ' +
      '브라우저 주소가 http://localhost:3000 인지 http://127.0.0.1:3000 인지에 따라 CORS 설정이 달라질 수 있습니다.';
    const cause = err instanceof Error ? err.message : String(err);
    throw new Error(`네트워크 요청 실패 (${url}): ${cause}. ${hint}`, {
      cause: err,
    });
  }

  if (!response.ok) {
    throw new ApiError(response.status);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  if (!text) {
    return undefined as T;
  }

  return JSON.parse(text) as T;
}
