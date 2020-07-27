/**
 * 
 */
 
 function sendInsertRequest () {
 	 $.ajax({
        type: "POST",
        url: "http://localhost:8080/users/addEvent",
        success: function (respond) {console.log("Correct")},
        error: function (respond) {
            console.log("Error");
            console.log(respond);
        }
    })
 }
 
 function sendDeleteRequest () {
 	
 	 var id = $('#id').val();
 
 	 $.ajax({
        type: "DELETE",
        url: "http://localhost:8080/users/deleteEvent/"+id,
        success: function (respond) {console.log("Correct")},
        error: function (respond) {
            console.log("Error");
            console.log(respond);
        }
    })
 }
