import {isBefore, set} from "date-fns";

export function greet(name: string) : string {
    const now = new Date();
    const h = now.getHours();

    // 23:00–02:59:59.999
    if (h >= 23 || h < 3) return "Go to bed... 😴";

    // 03:00–04:59:59.999
    if (h < 5) return "Its too late to sleep now 😭";

    // 05:00–11:59:59.999
    if (h < 12) return `Good morning, ${name}`;

    // 12:00:00.000–12:00:29.999
    const lunchCutoff = set(now, { hours: 12, minutes: 0, seconds: 30, milliseconds: 0 });
    if (h === 12 && isBefore(now, lunchCutoff)) return "Mmm, lunchtime 🍌";

    // 12:00:30–17:59:59.999
    if (h < 18) return `Good afternoon, ${name}`;

    // 18:00–22:59:59.999
    if (h < 23) return `Good evening, ${name}`;

    // Fallback (unreachable due to earlier guards)
    return `Good evening, ${name}`;
}