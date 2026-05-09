// TeamTrack — app.js
// CSRF + Drag & Drop Kanban (SortableJS) + Password strength indicator

// ================================================
// Récupère le token CSRF depuis les meta tags
// ================================================
function getCsrfToken() {
    var meta = document.querySelector('meta[name="_csrf"]');
    return meta ? meta.getAttribute('content') : '';
}

function getCsrfHeader() {
    var meta = document.querySelector('meta[name="_csrf_header"]');
    return meta ? meta.getAttribute('content') : 'X-XSRF-TOKEN';
}

/**
 * Gère l'affichage du message "Aucune tâche" dans les colonnes Kanban.
 */
function updateEmptyStates() {
    document.querySelectorAll('.kanban-cards').forEach(function (col) {
        var cardCount = col.querySelectorAll('.kanban-card').length;
        var emptyMsg  = col.querySelector('.kanban-empty');
        
        if (cardCount > 0) {
            if (emptyMsg) emptyMsg.style.display = 'none';
        } else {
            if (emptyMsg) emptyMsg.style.display = 'block';
        }
    });
}

document.addEventListener('DOMContentLoaded', function () {

    // Initialisation de l'état des colonnes vides
    updateEmptyStates();

    // ================================================
    // Drag & Drop Kanban avec SortableJS
    // ================================================
    var columns = document.querySelectorAll('.kanban-cards');
    
    if (columns.length > 0 && typeof Sortable !== 'undefined') {
        columns.forEach(function (col) {
            new Sortable(col, {
                group: 'kanban',
                animation: 150,
                ghostClass: 'kanban-ghost',
                chosenClass: 'kanban-chosen',
                dragClass: 'kanban-drag',
                
                // Déclenché quand une carte change de colonne (ajout ou retrait)
                onEnd: function() {
                    updateEmptyStates();
                },

                // Quand une carte est déposée dans une autre colonne
                onAdd: function (evt) {
                    var taskId    = evt.item.dataset.taskId;
                    var colId     = evt.to.getAttribute('id'); // "col-TODO", "col-IN_PROGRESS", "col-DONE"
                    var newStatus = colId.replace('col-', '');

                    // Récupère l'id du projet dans l'URL
                    var match = window.location.pathname.match(/\/projects\/(\d+)/);
                    if (!match || !taskId) return;
                    var projectId = match[1];

                    // Appel AJAX
                    var headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
                    headers[getCsrfHeader()] = getCsrfToken();

                    fetch('/projects/' + projectId + '/tasks/' + taskId + '/status/ajax', {
                        method: 'POST',
                        headers: headers,
                        body: 'status=' + newStatus
                    }).then(function (res) {
                        if (!res.ok) {
                            alert('Erreur lors du changement de statut.');
                            window.location.reload();
                        }
                    }).catch(function () {
                        window.location.reload();
                    });
                }
            });
        });
    }

    // ================================================
    // Auto-dismiss des alertes après 5 secondes
    // ================================================
    setTimeout(function () {
        document.querySelectorAll('.alert').forEach(function (alert) {
            alert.style.transition = 'opacity 0.6s ease';
            alert.style.opacity = '0';
            setTimeout(function () {
                if (alert.parentNode) alert.parentNode.removeChild(alert);
            }, 600);
        });
    }, 5000);

    // ================================================
    // Indicateur de force du mot de passe
    // ================================================
    var passwordInput = document.querySelector('input[name="password"]');
    var strengthBar   = document.getElementById('strength-bar');
    var strengthFill  = document.getElementById('strength-fill');
    var strengthLabel = document.getElementById('strength-label');

    if (passwordInput && strengthBar) {
        passwordInput.addEventListener('input', function () {
            var pwd = passwordInput.value;
            strengthBar.style.display = 'block';

            var score = 0;
            if (pwd.length >= 8)              score++;  
            if (pwd.length >= 12)             score++;  
            if (/[A-Z]/.test(pwd))            score++;  
            if (/[0-9]/.test(pwd))            score++;  
            if (/[^a-zA-Z0-9]/.test(pwd))     score++;  

            var percent = (score / 5) * 100;
            strengthFill.style.width = percent + '%';

            if (score <= 1) {
                strengthFill.style.background = '#ef4444';
                strengthLabel.textContent = 'Très faible';
                strengthLabel.style.color = '#ef4444';
            } else if (score === 2) {
                strengthFill.style.background = '#f59e0b';
                strengthLabel.textContent = 'Faible';
                strengthLabel.style.color = '#f59e0b';
            } else if (score === 3) {
                strengthFill.style.background = '#3b82f6';
                strengthLabel.textContent = 'Moyen';
                strengthLabel.style.color = '#3b82f6';
            } else if (score === 4) {
                strengthFill.style.background = '#22c55e';
                strengthLabel.textContent = 'Fort';
                strengthLabel.style.color = '#22c55e';
            } else {
                strengthFill.style.background = '#16a34a';
                strengthLabel.textContent = '✓ Très fort';
                strengthLabel.style.color = '#16a34a';
            }
        });
    }
});
