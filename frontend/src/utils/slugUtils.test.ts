import {describe, expect, test} from "vitest";
import {generateAutomaticSlug} from "./slugUtils.ts";

describe("generateAutomaticSlug", () => {
    test.each([
        ["support lowercase alphanumeric and hyphen", "abc1-2", "abc1-2"],
        ["convert uppercase to lowercase", "Abc", "abc"],
        ["convert spaces to hyphen", "ab c", "ab-c"],
        ["convert invalid to hyphen", "a,b.c(d", "a-b-c-d"],
        ["don't include leading hyphens", "-ab", "ab"],
        ["don't include trailing hyphens", "ab-", "ab"],
        ["squash multiple invalid characters", "a,.b", "a-b"],
        ["squash multiple hyphens", "a--b", "a-b"],
        ["squash multiple invalid and hyphens", "a-.b", "a-b"],

    ])("%s: '%s' -> '%s'", (_name: string, input: string, output: string) => {
        expect(generateAutomaticSlug(input)).equals(output)
    })
})