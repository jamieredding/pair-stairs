import {Card, CardContent, FormControl, Stack, TextField, Typography} from "@mui/material";
import {useState} from "react";
import ButtonRow from "../../ButtonRow.tsx";
import SaveButton from "../../SaveButton.tsx";

export function NewTeamPage() {
    const [name, setName] = useState<string>("")
    const [slug, setSlug] = useState<string>("")
    const slugOrPlaceholder = formatSafeForSlug(slug || name || "tbc")
    const baseUrl = buildBaseUrl()

    return (
        <main>
            <Card>
                <CardContent>
                    <Stack direction="column" gap={1}>
                        <Typography variant="h4">Create new team</Typography>
                        <div>
                            <TextField label="Name" variant="outlined" value={name}
                                       helperText="This is how your team name will appear in the UI"
                                       onChange={(e) => {
                                           const value = e.target.value;
                                           setName(value);
                                           setSlug(formatSafeForSlug(value))
                                       }}/>
                        </div>
                        <Stack direction="row" gap={1}>
                            <FormControl>
                                <TextField label="Slug" variant="outlined"
                                           helperText="This will be part of the url"
                                           value={slug} onChange={(e) => {
                                    setSlug(formatSafeForSlug(e.target.value));
                                }}/>
                            </FormControl>
                        </Stack>
                        <Stack direction="row" gap={1} alignItems="center">
                            <Typography variant="body1">Your team's url will
                                be: {baseUrl}/team/{slugOrPlaceholder}</Typography>
                        </Stack>
                        <ButtonRow>
                            <SaveButton/>
                        </ButtonRow>
                    </Stack>
                </CardContent>
            </Card>

        </main>
    )
}

function buildBaseUrl() {
    return window.location.protocol + "//" + window.location.host
}

function formatSafeForSlug(potentialSlug: string): string {
    return potentialSlug.toLowerCase()
        .replaceAll(/[^a-z0-9-]/g, "-")
    // todo what about multiple bad characters input, it shouldn't show multiple hyphens when generating (but maybe user is allowed?)
}