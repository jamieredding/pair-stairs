import {AlertTitle, Card, CardContent, Stack, TextField, Typography} from "@mui/material";
import ButtonRow from "../../ButtonRow.tsx";
import SaveButton from "../../SaveButton.tsx";
import {generateAutomaticSlug} from "../../../utils/slugUtils.ts";
import {createFormHook, createFormHookContexts} from "@tanstack/react-form";
import {z} from "zod";
import useAddTeam from "../../../hooks/teams/useAddTeam.ts";
import {useNavigate} from "@tanstack/react-router";
import type TeamDto from "../../../domain/TeamDto.ts";
import ErrorSnackbar from "../../ErrorSnackbar.tsx";
import type {ReactElement} from "react";
import FeatureFlag from "../../FeatureFlag.tsx";
import {teamsEnabled} from "../../../utils/featureFlags.ts";

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
    return (
        <FeatureFlag on={teamsEnabled}
                     showFeatureFlagFetching={true}
                     textWhenDisabled="Teams support is disabled."
        >
            <EnabledNewTeamPage/>
        </FeatureFlag>
    )
}

function EnabledNewTeamPage() {
    // `isLoading` is not read here as we're using `isSubmitting` from the form
    const {trigger, isError} = useAddTeam()
    const navigate = useNavigate()

    const form = useAppForm({
        defaultValues: {
            name: "",
            slug: "",
        },
        validators: {
            onChange: z.object({
                name: z.string()
                    .trim()
                    .nonempty("Name must not be empty")
                ,
                slug: z.string()
                    .regex(/^[a-z0-9-]+$/, "Slug must be lowercase a-z, 0-9, or -")
            })
        },
        onSubmit: async ({value}) => {
            const response: TeamDto = await trigger(value);
            if (response.slug) {
                await navigate({to: "/team/$teamName", params: {teamName: response.slug}})
            }
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
                                                         error={!field.state.meta.isValid}
                                                         helperText={
                                                             field.state.meta.isValid
                                                                 ? "This is how your team name will appear in the UI"
                                                                 : field.state.meta.errors.map(e => e?.message).join(", ")
                                                         }
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
                                                         error={!field.state.meta.isValid}
                                                         helperText={
                                                             field.state.meta.isValid
                                                                 ? "This will be part of the url"
                                                                 : field.state.meta.errors.map(e => e?.message).join(", ")
                                                         }
                                                         onChange={(e) => {
                                                             const value = e.target.value;
                                                             field.handleChange(value)
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
                                <form.Subscribe
                                    selector={(state) => [state.canSubmit, state.isSubmitting]}
                                    children={([canSubmit, isSubmitting]) => (
                                        <SaveButton onClick={form.handleSubmit} disabled={!canSubmit}
                                                    loading={isSubmitting}
                                        />
                                    )}
                                />

                            </ButtonRow>
                        </Stack>
                    </form>
                    <ErrorSnackbar error={isError} alertContent={alertContent}/>
                </CardContent>
            </Card>

        </main>
    )
}

function alertContent(errorCode: string): ReactElement {
    switch (errorCode) {
        case "INVALID_NAME":
            return <>
                <AlertTitle>Invalid name</AlertTitle>
                Name is not valid, please change it and try again.
            </>
        case "INVALID_SLUG":
            return <>
                <AlertTitle>Invalid slug</AlertTitle>
                Slug is not valid, please change it and try again.
            </>
        case "DUPLICATE_SLUG":
            return <>
                <AlertTitle>Slug already exists</AlertTitle>
                You cannot create a team with this slug as it already exists.
            </>
        default:
            return <>
                <AlertTitle>Unexpected error: {errorCode}</AlertTitle>
                Try again or refresh the page.
            </>
    }
}

function buildBaseUrl() {
    return window.location.protocol + "//" + window.location.host
}