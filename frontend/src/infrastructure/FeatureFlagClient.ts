import type FeatureFlagsDto from "../domain/FeatureFlagsDto.ts";
import {handleErrors} from "./handleErrors.ts";

export const FEATURE_FLAGS_PATH = "/api/v1/feature-flags"

export async function getFeatureFlags(url: string): Promise<FeatureFlagsDto> {
    const res = await handleErrors(await fetch(url));
    return await res.json();
}
