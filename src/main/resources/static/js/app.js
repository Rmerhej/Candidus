// Candidus : bouton de thème (clair / sombre), mémorisé sur l'appareil
document.addEventListener("DOMContentLoaded", () => {
    const root = document.documentElement;

    const themeActuel = () => {
        const force = root.getAttribute("data-theme");
        if (force) {
            return force;
        }
        return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
    };

    document.querySelectorAll("[data-theme-toggle]").forEach(button => {
        const majLibelle = () => {
            button.setAttribute("aria-label",
                themeActuel() === "dark" ? "Passer en mode clair" : "Passer en mode sombre");
        };
        majLibelle();
        button.addEventListener("click", () => {
            const nouveau = themeActuel() === "dark" ? "light" : "dark";
            root.setAttribute("data-theme", nouveau);
            try {
                localStorage.setItem("candidus-theme", nouveau);
            } catch (e) {
                // Navigation privée : le choix vaut pour cette page seulement
            }
            majLibelle();
        });
    });
});
