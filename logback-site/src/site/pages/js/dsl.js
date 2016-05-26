
var asGroovyURL='http://logback.qos.ch/translator/dsl/asGroovy';

function asGroovy(id) {

  var form = document.getElementById('aForm');
  if(form == null) {
    form = document.createElement("form");
    document.body.appendChild(form); 
  }
  var p = document.getElementById(id);
  
  var inner = p.innerHTML;
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
  form.setAttribute("action", asGroovyURL);
  
  var hiddenField = document.createElement("input");
  hiddenField.setAttribute("type", "hidden");
  hiddenField.setAttribute("name", "val");
  hiddenField.setAttribute("value", inner);
  form.appendChild(hiddenField);

  //alert("==="+inner);  
  form.submit();
  return false;
}

 
