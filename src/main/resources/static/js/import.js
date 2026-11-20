// Trouver des entreprises : « Tout cocher » et compteur sur le bouton
document.addEventListener("DOMContentLoaded", () => {
    const boxes = Array.from(document.querySelectorAll(".company-checkbox:not(:disabled)"));
    const selectAll = document.getElementById("select-all");
    const label = document.querySelector("[data-count-label]");

    const majBouton = () => {
        if (!label) {
            return;
        }
        const n = boxes.filter(box => box.checked).length;
        label.textContent = n === 0
            ? "Cochez les entreprises à ajouter"
            : "Ajouter " + n + (n > 1 ? " entreprises" : " entreprise") + " à mes candidatures";
    };

    boxes.forEach(box => box.addEventListener("change", majBouton));

    if (selectAll) {
        selectAll.addEventListener("change", () => {
            boxes.forEach(box => box.checked = selectAll.checked);
            majBouton();
        });
    }
    majBouton();
});
