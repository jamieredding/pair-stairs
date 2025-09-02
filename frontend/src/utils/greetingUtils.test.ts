import {afterEach, beforeEach, describe, expect, test, vi} from "vitest";
import {type DateValues, set} from "date-fns";
import {greet} from "./greetingUtils.ts";

interface Args {
    values: DateValues;
    expectedGreeting: string;
}

beforeEach(() => vi.useFakeTimers())
afterEach(() => vi.useRealTimers())

describe("greet", () => {
    const today = new Date()

    const args: Args[] = [
        {values: {hours: 5, minutes: 0, seconds: 0, milliseconds: 0}, expectedGreeting: "Good morning, Jamie"},
        {values: {hours: 11, minutes: 59, seconds: 59, milliseconds: 999}, expectedGreeting: "Good morning, Jamie"},
        {values: {hours: 12, minutes: 0, seconds: 0, milliseconds: 0}, expectedGreeting: "Mmm, lunchtime 🍌"},
        {values: {hours: 12, minutes: 0, seconds: 30, milliseconds: 0}, expectedGreeting: "Good afternoon, Jamie"},
        {values: {hours: 17, minutes: 59, seconds: 59, milliseconds: 999}, expectedGreeting: "Good afternoon, Jamie"},
        {values: {hours: 18, minutes: 0, seconds: 0, milliseconds: 0}, expectedGreeting: "Good evening, Jamie"},
        {values: {hours: 22, minutes: 59, seconds: 59, milliseconds: 999}, expectedGreeting: "Good evening, Jamie"},
        {values: {hours: 23, minutes: 0, seconds: 0, milliseconds: 0}, expectedGreeting: "Go to bed... 😴"},
        {values: {hours: 2, minutes: 59, seconds: 59, milliseconds: 999}, expectedGreeting: "Go to bed... 😴"},
        {values: {hours: 3, minutes: 0, seconds: 0, milliseconds: 0}, expectedGreeting: "Its too late to sleep now 😭"},
        {values: {hours: 4, minutes: 59, seconds: 59, milliseconds: 999}, expectedGreeting: "Its too late to sleep now 😭"},
    ]

    test.each(
        args.map(a => [set(today, a.values), a.expectedGreeting])
    )("returns greeting for date %s -> %s", (date: Date, expectedGreeting: string) => {
        vi.setSystemTime(date)
        expect(greet("Jamie")).toBe(expectedGreeting)
    })

})