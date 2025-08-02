import {expect, Page, test} from '@playwright/test';
import {format, subDays} from "date-fns";

test('test', async ({page}) => {
    await page.goto('/');

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

    await page.getByRole('button', {name: 'dev-0'}).click();
    await page.getByRole('button', {name: 'dev-1'}).click();
    await page.getByRole('button', {name: 'stream-a'}).click();
    await page.getByRole('button', {name: 'Add'}).click();

    await page.getByRole('button', {name: 'dev-2'}).click();
    await page.getByRole('button', {name: 'stream-b'}).click();
    await page.getByRole('button', {name: 'Add'}).click();

    await page.getByRole('button', {name: 'Save'}).click();

    // check that combination appeared in combination history
    const combinationHistoryCard = page.locator('div').filter({hasText: "Combination History"}).nth(1)

    const yesterdayCombination = combinationHistoryCard.locator("div").filter({hasText: "Yesterday"}).nth(4)
    await expect(yesterdayCombination).toBeVisible();

    const yesterdayPairStreams = yesterdayCombination.locator("tbody").locator("tr")
    await expect(yesterdayPairStreams.nth(0)).toContainText(["stream-a", "dev-0", "dev-1"].join(""))
    await expect(yesterdayPairStreams.nth(1)).toContainText(["stream-b", "dev-2"].join(""))

    // calculate a combination
    await page.getByRole('tab', {name: 'Calculate'}).click();
    await page.getByRole('button', {name: 'Next'}).click();
    await page.getByRole('button', {name: 'See combinations'}).click();

    await page.getByRole('button', {name: 'Choose'}).first().click();
    await page.getByRole('button', {name: 'Save'}).click();

    // check that combination appeared in combination history
    const todayCombination = combinationHistoryCard.locator("div").filter({hasText: "Today"}).nth(4)
    await expect(todayCombination).toBeVisible();

    const todayPairStreams = todayCombination.locator("tbody").locator("tr")
    await expect(todayPairStreams.nth(0)).toContainText(["stream-a", "dev-0"].join(""))
    await expect(todayPairStreams.nth(1)).toContainText(["stream-b", "dev-1", "dev-2"].join(""))

    // delete today's combination
    await page.locator('div').filter({hasText: /^Today$/}).getByRole('button').click();
    await page.getByRole('button', {name: 'Delete'}).click();

    // ensure it disappears
    await expect(todayCombination).not.toBeVisible();
});

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