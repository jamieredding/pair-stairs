import type FeatureFlagsDto from "../domain/FeatureFlagsDto.ts";

export const teamsEnabled =
    (flags: FeatureFlagsDto) => flags.teamsEnabled