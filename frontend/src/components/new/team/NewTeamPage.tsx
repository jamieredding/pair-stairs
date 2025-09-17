import {Card, CardContent, Stack, TextField, Typography} from "@mui/material";
import {useState} from "react";
import {CustomLink} from "../../CustomLink.tsx";
import ButtonRow from "../../ButtonRow.tsx";
import SaveButton from "../../SaveButton.tsx";

export function NewTeamPage() {
    const [name, setName] = useState<string>()
    const [slug, setSlug] = useState<string>()
    const slugOrPlaceholder = slug || "tbc"
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
                                       onChange={(e) => setName(e.target.value)}/>
                        </div>
                        <Stack direction="row" gap={1}>
                            <TextField label="Slug" variant="outlined" helperText="This will be part of the url"
                                       value={slug} onChange={(e) => setSlug(e.target.value)}/>
                        </Stack>
                        <Stack direction="row" gap={1} alignItems="center">
                            <Typography variant="body1">Your team's url will be:</Typography>
                            <CustomLink to={`/team/$teamName`} params={{"teamName": slugOrPlaceholder}}>
                                {baseUrl}/team/{slugOrPlaceholder}
                            </CustomLink>
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