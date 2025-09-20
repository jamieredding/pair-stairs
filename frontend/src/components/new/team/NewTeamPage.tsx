import {Card, CardContent, Stack, TextField, Typography} from "@mui/material";
import ButtonRow from "../../ButtonRow.tsx";
import SaveButton from "../../SaveButton.tsx";
import {generateAutomaticSlug} from "../../../utils/slugUtils.ts";
import {createFormHook, createFormHookContexts} from "@tanstack/react-form";
import {z} from "zod";

const {fieldContext, formContext} = createFormHookContexts()

const {useAppForm} = createFormHook({
    fieldComponents: {
        TextField
    },
    formComponents: {
        SaveButton
    },
    fieldContext,
    formContext
})

export function NewTeamPage() {
    const form = useAppForm({
        defaultValues: {
            name: "",
            slug: "",
        },
        validators: {
            onChange: z.object({
                name: z.string()
                // .trim()
                // .nonempty("Name must not be empty")
                ,
                slug: z.string()
                // .regex(/^[a-z0-9-]+$/, "Slug must be lowercase a-z, 0-9, or -")
            })
        },
        onSubmit: ({value}) => {
            console.log(JSON.stringify(value, null, 2))
        }
    })

    const baseUrl = buildBaseUrl()

    return (
        <main>
            <Card>
                <CardContent>
                    <form onSubmit={(e) => {
                        e.preventDefault()
                        form.handleSubmit()
                    }}>
                        <Stack direction="column" gap={1}>
                            <Typography variant="h4">Create new team</Typography>
                            <div>
                                <form.AppField
                                    name="name"
                                    children={(field) =>
                                        <field.TextField label="Name" variant="outlined" value={field.state.value}
                                                         helperText="This is how your team name will appear in the UI"
                                                         onChange={(e) => {
                                                             const value = e.target.value;
                                                             field.handleChange(value)
                                                             form.setFieldValue("slug", generateAutomaticSlug(value))
                                                         }}
                                        />}
                                />
                            </div>
                            <Stack direction="row" gap={1}>
                                <form.AppField
                                    name="slug"
                                    children={(field) =>
                                        <field.TextField label="Slug" variant="outlined" value={field.state.value}
                                                         helperText="This will be part of the url"
                                                         onChange={(e) => {
                                                             const value = e.target.value;
                                                             field.handleChange(generateAutomaticSlug(value))
                                                         }}
                                        />}
                                />
                            </Stack>
                            <Stack direction="row" gap={1} alignItems="center">
                                <form.Subscribe
                                    selector={(state) =>
                                        state.values.slug ||
                                        state.values.name ||
                                        "tbc"}
                                    children={(slugOrPlaceholder) => (
                                        <Typography variant="body1">Your team's url will
                                            be: {baseUrl}/team/{slugOrPlaceholder}</Typography>
                                    )}
                                />
                            </Stack>
                            <ButtonRow>
                                <SaveButton onClick={form.handleSubmit}/>
                            </ButtonRow>
                        </Stack>
                    </form>
                </CardContent>
            </Card>

        </main>
    )
}

function buildBaseUrl() {
    return window.location.protocol + "//" + window.location.host
}