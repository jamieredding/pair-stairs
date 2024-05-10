import {formatFriendlyDate} from './dateUtils';
import {describe, expect, it, test} from "vitest";
import {subDays} from "date-fns";


describe("formatFriendlyDate", () => {
    it("returns \"Today\" for today's date", () => {
        const date = new Date();
        expect(formatFriendlyDate(date)).equals("Today");
    });

    const today = new Date('2024-04-19');

    test.each([
        [0, 'Today'],
        [1, 'Yesterday'],
        [2, 'Wednesday'],
        [3, 'Tuesday'],
        [4, 'Monday'],
        [5, 'Last Sunday'],
        [6, 'Last Saturday'],
        [7, 'Last Friday'],
        [8, 'Last Thursday'],
        [9, 'Last Wednesday'],
        [10, 'Last Tuesday'],
        [11, 'Last Monday'],
        [12, '2024-04-07'],
    ])('returns correct string for %i days ago -> %s', (daysAgo: number, expected: string) => {
        const date = subDays(today, daysAgo);
        expect(formatFriendlyDate(date, today)).equals(expected);
    });
});