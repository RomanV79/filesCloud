// delete form
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

//rename form
var staticRename = document.getElementById('staticRename')
staticRename.addEventListener('show.bs.modal', function (event) {
    console.log(staticRename)
    // Button that triggered the modal
    const button = event.relatedTarget;
    // Extract info from attributes
    const name = button.getAttribute('data-name');
    // console.log('Name -> ' + name)

    // Update the modal's content.
    const oldName = staticRename.querySelector('input[name="old-name"]')
    oldName.value = name
})


// Upload and DragAdDrop files
// main part
const inputElement = document.querySelector('.dropzone-input');
const dropzone = document.querySelector('.dropzone');

dropzone.addEventListener("dragover", handleDragOver, false)

function handleDragOver(e) {
    e.preventDefault();
    dropzone.classList.add("dropzone-over");
}

["dragleave", "dragend"].forEach(type => {
    dropzone.addEventListener(type, evt => {
        dropzone.classList.remove("dropzone-over");
    })
})

// upload by click
dropzone.addEventListener("click", evt => {
    inputElement.click();
})
inputElement.addEventListener("change", evt => {
    console.log(evt)
    console.log(inputElement.files)
    if (inputElement.files.length) {
        sendFiles(inputElement.files)
        dropzoneMessage.innerHTML = "<span style='color: forestgreen'>Uploaded: " + inputElement.files.length + " file(s)</span>"
        setTimeout(function () {
            dropzoneMessage.textContent = "Drag and drop files here"
        }, 2000)
    }
})

// upload DragAndDrop
const dropzoneMessage = document.querySelector(".dropzone-message");

dropzone.addEventListener("drop", handleDrop, false)

function handleDrop(e) {
    e.preventDefault();
    // console.log(e.dataTransfer.files)

    if (e.dataTransfer.files.length) {
        inputElement.files = e.dataTransfer.files;
    }
    console.log(inputElement.files)
    sendFiles(inputElement.files)

    dropzone.classList.remove("dropzone-over")

    var countFiles = e.dataTransfer.files.length;
    // console.log("length = " + countFiles)
    dropzoneMessage.innerHTML = "<span style='color: forestgreen'>Uploaded: " + countFiles + " file(s)</span>"
    setTimeout(function () {
        dropzoneMessage.textContent = "Drag and drop files here"
    }, 2000)
}

function sendFiles(listFile) {

        for (let i = 0; i < listFile.length; i++) {
            const data = new FormData();
            data.append('file', listFile[i])
            fetch('api/v1/storage/upload', {
                method: 'POST',
                body: data
            }).then(data => {
                console.log('Server response:', data);
                if (i === listFile.length - 1) {
                    window.location.reload(true)
                }
            });
        }
    }


