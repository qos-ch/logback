

//   <h3><a name="LoggerContext" href="#LoggerContext"><span
//    class="anchor"/></a>Logger context</h3>

function decorate() {
  decoratePropertiesInTables();
  decorateDoAnchor();
  decorateConversionWordInTables();
}

function decoratePropertiesInTables() {
 //if(1==1) return;
 var elems = $('tr td:first-child span.prop');

 for(var i = 0; i < elems.length; i++) {
   var e = elems[i];
   var p = e.parentNode;
   if(p == null) continue;

   var tmpHTML = p.innerHTML;
   var propName = e.innerHTML;
   var nameAttr = $(e).attr('name')
   
   if(nameAttr == null) {
     var containerAttr = $(e).attr('container')
     if(containerAttr != null) 
       nameAttr = containerAttr+capitaliseFirstLetter(propName);
     else 
       nameAttr = propName;
   }
  
   p.innerHTML = "<a name='" + nameAttr + "' href='#" + nameAttr +
                "'><span class='anchor'/></a><b>" +tmpHTML +"</b>";
 } // for 
}

function decorateConversionWordInTables() {
 var elems = $('tr td.word');
 for(var i = 0; i < elems.length; i++) {
   var e = elems[i];
   var tmpHTML = e.innerHTML;
   var nameAttr = $(e).attr('name')
   if(nameAttr == null) 
     continue;
   e.innerHTML = "<a name='" + nameAttr + "' href='#" + nameAttr +
                "'><span class='anchor'/></a>" +tmpHTML;
 }
}


function decorateDoAnchor() {
   var elems = $('.doAnchor');
   for(var i = 0; i < elems.length; i++) {
     var e = elems[i];
     var tmpHTML = e.innerHTML;
     var nameAttr = $(e).attr('name')
     if(nameAttr == null) {
       nameAttr = camelCase($.trim(tmpHTML))
     }
     e.innerHTML = "<a name='" + nameAttr + "' href='#" + nameAttr +
                "'><span class='anchor'/></a>" +tmpHTML;
   }
} 

function capitaliseFirstLetter(str) {
  return str.charAt(0).toUpperCase() + str.slice(1);
}


function camelCase(str) {  
  var res = str.trim().replace(/\s\w/g, function(match) {
              return match.trim().toUpperCase();
            });
  return res;
}

