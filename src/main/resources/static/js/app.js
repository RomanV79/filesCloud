var staticDelete = document.getElementById('staticDelete')
staticDelete.addEventListener('show.bs.modal', function (event) {

    // Button that triggered the modal
    const button = event.relatedTarget;
    // Extract info from attributes
    const name = button.getAttribute('data-name');

    // Update the modal's content.
    const modalBodyInput = staticDelete.querySelector('.modal-body input');
    const urlParams = new URLSearchParams(window.location.search);
    const path = urlParams.get('path');

    modalBodyInput.value = path + name
})

var staticRename = document.getElementById('staticRename')
staticRename.addEventListener('show.bs.modal', function (event) {
    console.log(staticRename)
    // Button that triggered the modal
    const button = event.relatedTarget;
    // Extract info from attributes
    const name = button.getAttribute('data-name');
    console.log('Name -> ' + name)

    // Update the modal's content.
    const oldName = staticRename.querySelector('input[name="old-name"]')
    oldName.value = name
})