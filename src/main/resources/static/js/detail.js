// Fiche candidature : bouton "Copier le texte" et indicateur pendant la préparation par l'IA
document.addEventListener("DOMContentLoaded", () => {

    document.querySelectorAll("[data-copy]").forEach(button => {
        button.addEventListener("click", async () => {
            const field = document.querySelector(button.dataset.copy);
            if (!field) {
                return;
            }
            try {
                await navigator.clipboard.writeText(field.value);
                button.textContent = "Texte copié ✓";
            } catch (e) {
                field.select();
                button.textContent = "Sélectionné : faites Ctrl+C";
            }
        });
    });

    // L'IA peut mettre quelques secondes : on l'indique et on évite les doubles clics
    document.querySelectorAll("form[data-loading]").forEach(form => {
        form.addEventListener("submit", () => {
            const button = form.querySelector("button[type=submit]");
            if (button) {
                button.disabled = true;
                button.textContent = "Préparation en cours…";
            }
        });
    });
});
