
//var xml2CanonURL='https://logback.qos.ch/translator/dsl/xml2Canon/asText';
var xml2CanonURL='/translator/dsl/xml2Canon/asText';

//var xml2CanonURL='http://localhost:8080/translator/dsl/xml2Canon/asText';

function canonical(legacyId, canonicalId) {

    var form = document.getElementById('aForm');
    if(form == null) {
        form = document.createElement("form");        
        document.body.appendChild(form); 
    }

    $(form).empty();
    form.id = 'aForm';    
    
    var legacyElement = document.getElementById(legacyId);
    
    var inner = legacyElement.innerHTML;
    //alert("==="+inner);
    inner = inner.replace(/<pre class="prettyprint source">/gi, '');
    inner = inner.replace(/<\/pre>/gi, '');
    inner = inner.replace(/&lt;/gi, '<');
    inner = inner.replace(/&gt;/gi, '>');
    
    inner = inner.replace(/<span class="[^"]*"?>/gi, '');
    inner = inner.replace(/<\/span>/gi, '');
    inner = inner.replace(/<br>/gi, '');
    inner = inner.replace(/&nbsp;/gi, '');
    inner = inner.replace(/<b>/gi, '');
    inner = inner.replace(/<\/b>/gi, '');
    
    form.setAttribute("method", "post");
    form.setAttribute("action", xml2CanonURL);
    
    var hiddenField = document.createElement("input");
    hiddenField.setAttribute("type", "hidden");
    hiddenField.setAttribute("name", "val");
    hiddenField.setAttribute("value", inner);
    form.appendChild(hiddenField);
    
    var postData = $("#aForm").serialize();  

    var canonicalElement = document.getElementById(canonicalId);

    
    $.post(xml2CanonURL, postData, function(payload, status) {
            payload = '<pre class="source prettyprint">'+payload+'</pre>'
            canonicalElement.innerHTML = payload;
            canonicalElement.innerHTML = prettyPrintOne(canonicalElement.innerHTML);
          });
    

}

//   $.ajax({
//       url: xml2CanonURL, 
//       type: 'POST',
//       crossDomain: true,
//       headers: {'Referrer-Policy': 'origin-when-cross-origin',
//                 'Access-Control-Allow-Origin': '*',
//                 'Access-Control-Allow-Credentials': 'true',
//                 'Access-Control-Allow-Methods': 'POST, OPTIONS'},
//       //beforeSend: function(xhr){
//       //    xhr.setRequestHeader('Access-Control-Allow-Origin', '*');
//       //    xhr.setRequestHeader('Access-Control-Allow-Credentials', 'true');
//       //    xhr.setRequestHeader('Access-Control-Allow-CredentialsMethods', 'POST');            
//       //},
//       //xhrFields: { withCredentials: true },                     
//       data: inner,
//       success: function(res) {
//           alert(res);
//       }
//
//   } );
//  alert("zzzzz");
//  form.submit( function (event) {
//    $.ajax({
//            url: xml2CanonURL, 
//            type: 'POST',
//            contents: 'text/plain; charset=UTF-8',
//            processData: false,
//            dataType: "text",
//            data: $(this).serialize(), 
//            success: function(payload, status) {
//                alert(payload);
//            }
//    });
//      return false;  
//      //event.preventDefault();  
//    });
//   return false;
//}


function xxCcanonical(id) {

  var form = document.getElementById('aForm');
  if(form == null) {
    form = document.createElement("form");
    document.body.appendChild(form); 
  }
  var p = document.getElementById(id);
  
  var inner = legacyElementinnerHTML;
  //alert("==="+inner);  
  inner = inner.replace(/&lt;/gi, '<');
  inner = inner.replace(/&gt;/gi, '>');

  inner = inner.replace(/<span class="[^"]*"?>/gi, '');
  inner = inner.replace(/<\/span>/gi, '');
  inner = inner.replace(/<br>/gi, '');
  inner = inner.replace(/&nbsp;/gi, '');
  inner = inner.replace(/<b>/gi, '');
  inner = inner.replace(/<\/b>/gi, '');

  form.setAttribute("method", "post");
  form.setAttribute("action", xml2CanonURL);
  
  var hiddenField = document.createElement("input");
  hiddenField.setAttribute("type", "hidden");
  hiddenField.setAttribute("name", "val");
  hiddenField.setAttribute("value", inner);
  form.appendChild(hiddenField);

  //alert("==="+inner);  
  form.submit();
  return false;
}

 
