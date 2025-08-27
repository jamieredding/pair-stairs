import type {Displayable} from "../domain/Displayable.tsx";

export function sorted(displayables: Displayable[]): Displayable[] {
    return [...displayables]
        .sort((a, b) =>
            a.displayName.localeCompare(b.displayName)
        )
}