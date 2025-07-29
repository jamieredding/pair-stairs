function openDialog() {
    document.getElementById("add-developer-dialog").showModal()
}

async function handleSubmitDeveloper(event) {
    event.preventDefault();

    const form = document.forms["add-developer-form"];
    const name = form["name"].value;

    try {
        // 1. POST new developer
        const postResponse = await fetch("/api/v1/developers", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name: name }),
        });

        if (!postResponse.ok) throw new Error("Failed to add developer");

        // 2. GET updated list
        const getResponse = await fetch("/api/v1/developers/info");
        if (!getResponse.ok) throw new Error("Failed to fetch developer list");

        const developerInfos = await getResponse.json();

        // 3. Replace <ul> contents
        const list = document.getElementById("developers-list");
        list.innerHTML = "";

        developerInfos.forEach(developerInfo => {
            const li = document.createElement("li");
            li.textContent = developerInfo.displayName;
            list.appendChild(li);
        });

        // 4. Close dialog
        document.getElementById("add-developer-dialog").close();
        document.getElementById("add-developer-form").reset();
    } catch (error) {
        // todo should be a toast of some kind
        alert("Something went wrong: " + error.message);
    }
}

window.addEventListener("load", () => {

    const dialog = document.querySelector("#add-developer-dialog");
    const form = document.querySelector("#add-developer-form")

    function clickWasWithinDialog(event) {
        const rect = dialog.getBoundingClientRect();

        return event.clientX >= rect.left &&
            event.clientX <= rect.right &&
            event.clientY >= rect.top &&
            event.clientY <= rect.bottom;
    }

    function handleClickDialog(event) {
        if (!clickWasWithinDialog(event)) {
            dialog.close();
        }
    }

    dialog.addEventListener("click", handleClickDialog);
    form.addEventListener("submit", handleSubmitDeveloper);
});