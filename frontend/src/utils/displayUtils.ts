import type {Displayable} from "../domain/Displayable.tsx";

export function sorted<T extends Displayable>(displayables: T[]): T[] {
    return [...displayables]
        .sort((a, b) =>
            a.displayName.localeCompare(b.displayName)
        )
}