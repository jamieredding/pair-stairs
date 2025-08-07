import {endOfWeek, format, isSameDay, isSameWeek, startOfWeek, type StartOfWeekOptions, subDays} from "date-fns";

const weekStartsOnMonday : StartOfWeekOptions = {weekStartsOn: 1};

export function formatFriendlyDate(date: Date, today: Date = new Date()): string {
    if (isSameDay(date, today)) {
        return "Today";
    }

    if (isSameDay(date, subDays(today, 1))) {
        return "Yesterday";
    }

    if (isSameWeek(date, today, {weekStartsOn: 1})) {
        return format(date, "EEEE");
    }

    const lastWeek = subDays(today, 7);
    const startOfLastWeek = startOfWeek(lastWeek, weekStartsOnMonday);
    const endOfLastWeek = endOfWeek(lastWeek, weekStartsOnMonday);

    if (date >= startOfLastWeek && date <= endOfLastWeek) {
        return `Last ${format(date, "EEEE")}`;
    }

    return format(date, "yyyy-MM-dd");
}