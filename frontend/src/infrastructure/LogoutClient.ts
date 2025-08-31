interface TokenResponse {
    token: string;
    headerName: string;
}

export async function logout() {
    const response = await fetch("/csrf")
    const tokenResponse: TokenResponse = await response.json()
    const headers: HeadersInit = {}
    headers[tokenResponse.headerName] = tokenResponse.token

    return fetch("/logout", {
        method: "POST",
        headers: headers,
        redirect: "manual"
    })
        .then(() => window.location.assign("/"))
    // todo response handling
}
