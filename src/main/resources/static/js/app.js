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
["dragleave", "dragend"]. forEach(type => {
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
    if (inputElement.files.length) {
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

    dropzone.classList.remove("dropzone-over")

    var countFiles = e.dataTransfer.files.length;
    // console.log("length = " + countFiles)
    dropzoneMessage.innerHTML = "<span style='color: forestgreen'>Uploaded: " + countFiles + " file(s)</span>"
    setTimeout(function () {
        dropzoneMessage.textContent = "Drag and drop files here"
    }, 2000)
}


// document.querySelectorAll(".dropzone-input").forEach(inputElement => {
//     const dropZoneElement = inputElement.closest(".dropzone");
//
//     dropZoneElement.addEventListener('dragover', evt => {
//         dropZoneElement.classList.add('.dropzone-over');
//     })
// })


// dropzone.addEventListener()
// dropzone.addEventListener('dragover', handleDragOver, false);
// dropzone.addEventListener('drop', handleDrop, false);



// function handleDragOver(event) {
//     console.log("over box")
//     event.stopPropagation();
//     event.preventDefault();
//     event.dataTransfer.dropEffect = 'copy'; // Visual cue that files can be dropped
// }

// function handleDrop(event) {
//     event.stopPropagation();
//     event.preventDefault();
//     var files = event.dataTransfer.files; // List of dropped files
//     var data = new FormData();
//     for (var i = 0; i < files.length; i++) {
//         data.append('file[]', files[i]); // Add each file to FormData object
//     }
//     // Use AJAX to upload files to server
//     $.ajax({
//         url: '#',
//         type: 'POST',
//         data: data,
//         processData: false,
//         contentType: false,
//         success: function(response) {
//             console.log(response);
//         },
//         error: function(xhr, status, error) {
//             console.log(error);
//         }
//     });
// }


