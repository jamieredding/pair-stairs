class Navbar extends HTMLElement {
    // noinspection JSUnusedGlobalSymbols
    connectedCallback() {
        if (this.querySelector("header")) return;

        this.innerHTML = `
        <header class="h-stack">
            <div class="h-stack">
                <h1><a class="index-link" href="/">pair-stairs</a></h1>
                <a href="/developers">Developers</a>
                <a href="/streams">Streams</a>
            </div>
        </header>
        `
    }
}

export const registerNavbar = () => customElements.define("x-navbar", Navbar)