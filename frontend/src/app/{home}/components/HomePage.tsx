import {Card, CardContent, Stack, Typography} from "@mui/material";
import CombinationEventDto from "@/domain/CombinationEventDto";
import CombinationTable from "@/components/CombinationTable";
import {formatFriendlyDate} from "@/utils/dateUtils";
import {parse} from "date-fns";

const combinationEvents: CombinationEventDto[] = [
    {
        id: 1,
        date: "2024-05-10",
        combination: [
            {
                developers: [
                    {id: 0, displayName: "dev-0"},
                    {id: 1, displayName: "dev-1"},
                ],
                stream:
                    {id: 1, displayName: "stream-a"}

            },
            {
                developers: [
                    {id: 2, displayName: "dev-2"},
                ],
                stream:
                    {id: 2, displayName: "stream-b"}

            }
        ]
    },

    {
        id: 2,
        date: "2024-05-09",
        combination: [
            {
                developers: [
                    {id: 0, displayName: "dev-0"},
                    {id: 2, displayName: "dev-2"},
                ],
                stream:
                    {id: 1, displayName: "stream-a"}

            },
            {
                developers: [
                    {id: 1, displayName: "dev-1"},
                ],
                stream:
                    {id: 2, displayName: "stream-b"}

            }
        ]
    },

    {
        id: 3,
        date: "2024-05-08",
        combination: [
            {
                developers: [
                    {id: 1, displayName: "dev-1"},
                    {id: 2, displayName: "dev-2"},
                ],
                stream:
                    {id: 1, displayName: "stream-a"}

            },
            {
                developers: [
                    {id: 0, displayName: "dev-0"},
                ],
                stream:
                    {id: 2, displayName: "stream-b"}

            }
        ]
    },
];

export default function HomePage() {
    return (
        <main>
            <Typography variant="h4">Combination Events</Typography>
            <Stack gap={1}>
                {combinationEvents.map((combinationEvent) =>
                    <Card key={combinationEvent.id}>
                        <CardContent>
                            <Stack gap={1}>
                                <Typography variant="h5">{formatFriendlyDate(parse(combinationEvent.date, "yyyy-MM-dd", new Date()))}</Typography>
                                <CombinationTable combination={combinationEvent.combination}/>
                            </Stack>
                        </CardContent>
                    </Card>
                )}
            </Stack>
        </main>
    );
}