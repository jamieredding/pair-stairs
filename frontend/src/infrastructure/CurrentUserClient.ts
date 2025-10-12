import type CurrentUserDto from "../domain/CurrentUserDto.ts";
import {handleErrors} from "./handleErrors.ts";

export const CURRENT_USER_PATH = "/api/v1/me"

export async function getCurrentUser(url: string): Promise<CurrentUserDto> {
    const res = await handleErrors(await fetch(url));
    return await res.json();
}
