# Frontend

## Tech Stack
- [Typescript](https://www.typescriptlang.org/) (language)
- [React](https://react.dev/) (UI component framework)
- [Tanstack-router](https://tanstack.com/router/latest) (for routing)
- [Vite](https://vite.dev/) (build tool)
- [MUI](https://mui.com/) (React component library)

## Development
First, start the local wiremock server to act as a replacement for the backend API during development:

```shell
npm run wiremock
```

Once this is running, start the local development server to see the frontend:

```shell
npm run dev
```

Then go to [http://localhost:5173/](http://localhost:5173/)

If you make a change in the frontend source code and save, e.g. [HomePage.tsx](src/components/index/HomePage.tsx),
you should see the changes happen live in the browser.

### Auth
If you are doing any work around login, you will also need to have a running backend.

Run `make start-services` to bring up a database and oauth server for the backend to use.

Then run an instance of the backend using the `Application (vite development)` IntelliJ run configuration.

## Packaging

This is a static single page application that is packaged into a `/static` directory as a maven dependency that
the backend can pull in and serve as static content via spring boot.

The packaging is done as part of the maven build and all relevant configuration can be found in the [pom.xml](pom.xml).