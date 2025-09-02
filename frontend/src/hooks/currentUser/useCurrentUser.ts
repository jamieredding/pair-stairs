import useSWR, {type SWRResponse} from "swr";
import {CURRENT_USER_PATH, getCurrentUser} from "../../infrastructure/CurrentUserClient.ts";
import type CurrentUserDto from "../../domain/CurrentUserDto.ts";

export default function useCurrentUser() {
    const {data, error, isLoading}: SWRResponse<CurrentUserDto> =
        useSWR(CURRENT_USER_PATH, getCurrentUser)

    return {
        currentUser: data,
        isLoading,
        isError: error
    }
}