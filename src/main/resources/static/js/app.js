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

// Accueil : l'histoire de candidus se dévoile mot à mot au défilement.
// Sans JavaScript, ou si l'appareil demande moins d'animations, le texte reste simplement affiché.
document.addEventListener("DOMContentLoaded", () => {
    const bloc = document.querySelector("[data-reveal]");
    if (!bloc || window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
        return;
    }

    // Découpe chaque texte en mots, sans toucher à la structure (le manuscrit garde sa police)
    const decouper = noeud => {
        Array.from(noeud.childNodes).forEach(enfant => {
            if (enfant.nodeType === Node.TEXT_NODE) {
                const fragment = document.createDocumentFragment();
                enfant.textContent.split(/(\s+)/).forEach(morceau => {
                    if (morceau.trim() === "") {
                        fragment.appendChild(document.createTextNode(morceau));
                    } else {
                        const span = document.createElement("span");
                        span.className = "mot";
                        span.textContent = morceau;
                        fragment.appendChild(span);
                    }
                });
                enfant.replaceWith(fragment);
            } else if (enfant.nodeType === Node.ELEMENT_NODE) {
                decouper(enfant);
            }
        });
    };
    decouper(bloc);
    bloc.classList.add("est-anime");

    const mots = Array.from(bloc.querySelectorAll(".mot"));
    const maj = () => {
        const rect = bloc.getBoundingClientRect();
        const hauteur = window.innerHeight;
        // 0 quand le haut du bloc arrive aux 3/4 de l'écran, 1 quand son bas atteint le milieu
        const progression = (hauteur * 0.75 - rect.top) / (rect.height + hauteur * 0.25);
        const nb = Math.round(Math.min(Math.max(progression, 0), 1) * mots.length);
        mots.forEach((mot, i) => mot.classList.toggle("vu", i < nb));
    };
    window.addEventListener("scroll", maj, { passive: true });
    window.addEventListener("resize", maj);
    maj();
});
