import {expect, Page, test} from '@playwright/test';
import {format, subDays} from "date-fns";

const USERNAME = 'admin@example.com';
const PASSWORD = 'password';

test('acceptance test', async ({page}) => {
    await page.goto('/');

    await login(page)

    // create some developers
    await page.getByRole('link', {name: 'Developers'}).click();

    await createDeveloper(page, 'dev-0');
    await createDeveloper(page, 'dev-1');
    await createDeveloper(page, 'dev-2');

    // create some streams
    await page.getByRole('link', {name: 'Streams'}).click();

    await createStream(page, 'stream-a');
    await createStream(page, 'stream-b');

    await page.getByRole('link', {name: 'pair-stairs'}).click();

    // see that calculate form contains developers
    await expect(page.getByLabel("Calculate")).toContainText('dev-0');
    await expect(page.getByLabel("Calculate")).toContainText('dev-1');
    await expect(page.getByLabel("Calculate")).toContainText('dev-2');

    await page.getByRole('tab', {name: 'Manual'}).click();

    // choose a manual combination for yesterday
    await chooseYesterdayDate(page);

    await page.getByText('dev-0').click();
    await page.getByText('dev-1').click();
    await page.getByText('stream-a').click();
    await page.getByRole('button', {name: 'Add'}).click();

    await page.getByText('dev-2').click();
    await page.getByText('stream-b').click();
    await page.getByRole('button', {name: 'Add'}).click();

    await page.getByRole('button', {name: 'Save'}).click();

    // check that combination appeared in combination history
    const combinationHistoryCard = page.locator(".MuiCard-root", {
        has: page.getByRole("heading", {level: 2, name: /^Combination History$/})
    });

    const yesterdayISO  = format(subDays(new Date(), 1), 'yyyy-MM-dd');
    const yesterdayCombination = combinationHistoryCard.locator(".MuiCard-root", {
        has: page.getByRole("heading", {level: 3, name: yesterdayISO})
    })
    await expect(yesterdayCombination).toBeVisible();

    const yesterdayPairStreams = yesterdayCombination.getByRole("row")
    await expect(yesterdayPairStreams.nth(1)).toContainText(["stream-a", "dev-0", "dev-1"].join(""))
    await expect(yesterdayPairStreams.nth(2)).toContainText(["stream-b", "dev-2"].join(""))

    // calculate a combination
    await page.getByRole('tab', {name: 'Calculate'}).click();
    await page.getByRole('button', {name: 'See combinations'}).click();

    await page.getByRole('button', {name: 'Choose'}).first().click();
    await page.getByRole('button', {name: 'Save'}).click();

    // check that combination appeared in combination history
    const todayISO = format(new Date(), 'yyyy-MM-dd');
    const todayCombination = combinationHistoryCard.locator(".MuiCard-root", {
        has: page.getByRole("heading", {level: 3, name: todayISO})
    })
    await expect(todayCombination).toBeVisible();

    const todayPairStreams = todayCombination.getByRole("row")
    await expect(todayPairStreams.nth(1)).toContainText(["stream-a", "dev-0"].join(""))
    await expect(todayPairStreams.nth(2)).toContainText(["stream-b", "dev-1", "dev-2"].join(""))

    // delete today's combination
    await page.locator('div').filter({hasText: /^Today$/}).getByRole('button').click();
    await page.getByRole('button', {name: 'Delete'}).click();

    // ensure it disappears
    await expect(todayCombination).not.toBeVisible();

    // logout
    await page.getByRole('link', { name: 'Log out' }).click();
    await expect(page.getByRole('heading', { name: 'Log in to Your Account' })).toBeVisible();
});

test('redirect when not logged in', async ({page}) => {
    await page.goto('/developers');

    await expect(page.getByRole('heading', { name: 'Log in to Your Account' })).toBeVisible();
})

async function login(page: Page) {
    await expect(page.getByRole('heading', { name: 'Log in to Your Account' })).toBeVisible();
    await page.getByPlaceholder('email address').click();
    await page.getByPlaceholder('email address').fill(USERNAME);
    await page.getByPlaceholder('password').click();
    await page.getByPlaceholder('password').fill(PASSWORD);
    await page.getByRole('button', { name: 'Login' }).click();
    await page.getByRole('button', { name: 'Grant Access' }).click();
}

async function createDeveloper(page: Page, name: string) {
    await page.getByRole('button', {name: 'New developer'}).click();
    await expect(page.getByRole('heading', {name: 'Add new developer'})).toBeVisible();
    await page.getByLabel('Name').click();
    await page.getByLabel('Name').fill(name);
    await page.getByRole('button', {name: 'Save'}).click();
    await expect(page.getByRole('listitem').filter({hasText: name})).toBeVisible();
}

async function createStream(page: Page, name: string) {
    await page.getByRole('button', {name: 'New stream'}).click();
    await expect(page.getByRole('heading', {name: 'Add new stream'})).toBeVisible();
    await page.getByLabel('Name').click();
    await page.getByLabel('Name').fill(name);
    await page.getByRole('button', {name: 'Save'}).click();
    await expect(page.getByRole('listitem').filter({hasText: name})).toBeVisible();
}

async function chooseYesterdayDate(page: Page) {
    const yesterdayDate = subDays(new Date(), 1)
    const yesterdayDateFormat = format(yesterdayDate, "yyyyMMdd")

    await page.getByRole('group', { name: 'Date of combination' }).click();
    await page.keyboard.type(yesterdayDateFormat)
}