import type FeatureFlagsDto from "../domain/FeatureFlagsDto.ts";
import {type ReactNode} from "react";
import useFeatureFlags from "../hooks/featureFlags/useFeatureFlags.ts";
import Loading from "./Loading.tsx";
import Error from "./Error.tsx";
import {Typography} from "@mui/material";


export interface FeatureFlagProps {
    on: (flags: FeatureFlagsDto) => boolean;
    children: ReactNode;
    showFeatureFlagFetching?: boolean;
    textWhenDisabled?: string;
}

export default function FeatureFlag({
                                        on,
                                        children,
                                        showFeatureFlagFetching = false,
                                        textWhenDisabled = undefined
                                    }: FeatureFlagProps) {
    const {featureFlags, isLoading, isError} = useFeatureFlags()

    return (
        <>
            {showFeatureFlagFetching &&
                <>
                    {isLoading && <Loading/>}
                    {isError && <Error/>}
                </>
            }
            {(featureFlags && on(featureFlags))
                ? children
                : textWhenDisabled && <Typography variant="h4" color="error">{textWhenDisabled}</Typography>
            }
        </>
    )
}