/**
 * Formats either a date-only value ("2026-11-10", shown as that calendar day in every
 * timezone) or a full timestamp ("2026-11-03T07:20:10Z", shown in the viewer's timezone).
 */
export function formatDate(value: string): string {
  const options: Intl.DateTimeFormatOptions = { day: 'numeric', month: 'short', year: 'numeric' }
  if (value.includes('T')) return new Date(value).toLocaleDateString(undefined, options)

  const [year, month, day] = value.split('-').map(Number)
  return new Date(year, month - 1, day).toLocaleDateString(undefined, options)
}
