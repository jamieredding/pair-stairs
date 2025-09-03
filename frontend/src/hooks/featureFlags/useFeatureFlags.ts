import useSWR, {type SWRResponse} from "swr";
import type FeatureFlagsDto from "../../domain/FeatureFlagsDto.ts";
import {FEATURE_FLAGS_PATH, getFeatureFlags} from "../../infrastructure/FeatureFlagClient.ts";

export default function useFeatureFlags() {
    const {data, error, isLoading}: SWRResponse<FeatureFlagsDto> =
        useSWR(FEATURE_FLAGS_PATH, getFeatureFlags)

    return {
        featureFlags: data,
        isLoading,
        isError: error
    }
}