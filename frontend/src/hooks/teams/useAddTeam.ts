import useSWRMutation, {type SWRMutationResponse} from "swr/mutation";
import type TeamDto from "../../domain/TeamDto.ts";
import {addTeam} from "../../infrastructure/TeamClient.ts";
import type {ApiError} from "../../domain/ApiError.ts";

export default function useAddTeam() {
    const {trigger, error, isMutating}: SWRMutationResponse<TeamDto, ApiError, "/api/v1/teams", TeamDto> =
        useSWRMutation("/api/v1/teams", addTeam);

    return {trigger, isError: error, isLoading: isMutating}
}