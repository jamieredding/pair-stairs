export function generateAutomaticSlug(potentialSlug: string): string {
    return potentialSlug.toLowerCase()
        .replaceAll(/[^a-z0-9-]/g, "-")
        .replaceAll(/-+/g, "-")
        .replaceAll(/^-|-$/g, "")
}