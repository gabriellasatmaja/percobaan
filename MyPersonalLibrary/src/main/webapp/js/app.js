// My Personal Library — app.js
// Minimal JavaScript — the app works without JS; JS only enhances UX.

// Auto-dismiss flash messages after 4 seconds
document.addEventListener('DOMContentLoaded', function () {
    var alerts = document.querySelectorAll('.alert-success');
    alerts.forEach(function (el) {
        setTimeout(function () {
            el.style.transition = 'opacity 0.5s';
            el.style.opacity = '0';
            setTimeout(function () { el.remove(); }, 500);
        }, 4000);
    });

    // Current date default for log_date inputs
    var dateInputs = document.querySelectorAll('input[type="date"][id="log_date"]');
    dateInputs.forEach(function (inp) {
        if (!inp.value) {
            var today = new Date();
            var y = today.getFullYear();
            var m = String(today.getMonth() + 1).padStart(2, '0');
            var d = String(today.getDate()).padStart(2, '0');
            inp.value = y + '-' + m + '-' + d;
        }
    });
});
