import type CurrentUserDto from "../domain/CurrentUserDto.ts";

export const CURRENT_USER_PATH = "/api/v1/me"

export async function getCurrentUser(url: string): Promise<CurrentUserDto> {
    const res = await fetch(url);
    return await res.json();
}
