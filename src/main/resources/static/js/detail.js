// Fiche candidature : copier la lettre, confirmer la suppression, patienter pendant l'IA
document.addEventListener("DOMContentLoaded", () => {

    document.querySelectorAll("[data-copy]").forEach(button => {
        const libelle = button.innerHTML;
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
                button.textContent = "Texte sélectionné : copiez-le";
            }
            setTimeout(() => { button.innerHTML = libelle; }, 2500);
        });
    });

    document.querySelectorAll("form[data-confirm]").forEach(form => {
        form.addEventListener("submit", event => {
            if (!window.confirm(form.dataset.confirm)) {
                event.preventDefault();
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
