import {Card, CardContent, FormControl, Stack, TextField, Typography} from "@mui/material";
import {useState} from "react";
import ButtonRow from "../../ButtonRow.tsx";
import SaveButton from "../../SaveButton.tsx";
import {generateAutomaticSlug} from "../../../utils/slugUtils.ts";

export function NewTeamPage() {
    const [name, setName] = useState<string>("")
    const [slug, setSlug] = useState<string>("")
    const slugOrPlaceholder = generateAutomaticSlug(slug || name || "tbc")
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
                                           setSlug(generateAutomaticSlug(value))
                                       }}/>
                        </div>
                        <Stack direction="row" gap={1}>
                            <FormControl>
                                <TextField label="Slug" variant="outlined"
                                           helperText="This will be part of the url"
                                           value={slug} onChange={(e) => {
                                    setSlug(generateAutomaticSlug(e.target.value));
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