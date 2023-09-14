
function del (clicked_id){
    console.log(clicked_id);
    console.log(document.getElementById(clicked_id).textContent);
    const urlParams = new URLSearchParams(window.location.search);
    const path = urlParams.get('path');
    console.log(path)
}

var staticDelete = document.getElementById('staticDelete')

staticDelete.addEventListener('show.bs.modal', function (event) {

    // Button that triggered the modal
    const button = event.relatedTarget;
    // Extract info from data-bs-* attributes
    const name = button.getAttribute('data-name');

    // Update the modal's content.
    var modalBodyInput = staticDelete.querySelector('.modal-body input');
    const urlParams = new URLSearchParams(window.location.search);
    const path = urlParams.get('path');

    modalBodyInput.value = path + name
})