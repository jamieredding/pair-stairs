import useSWR, {type SWRResponse} from "swr";
import type {ApiError} from "../../domain/ApiError.ts";
import {getTeams} from "../../infrastructure/TeamClient.ts";
import type TeamDto from "../../domain/TeamDto.ts";

export default function useTeams() {
    const {data, error, isLoading}: SWRResponse<TeamDto[], ApiError> =
        useSWR("/api/v1/teams", getTeams)

    return {
        allTeams: data,
        isLoading,
        isError: error
    }
}