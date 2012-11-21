
var BASE_LOCAL_PATH = 'file:///android_asset/template/';

function displayTextile(textileSrc,elementId) {
		var html = convert(textileSrc);
		document.getElementById(elementId).innerHTML=html;
	}


$(document).bind('pagechange', function() {
  $('.ui-page-active .ui-listview').listview('refresh');
  $('.ui-page-active :jqmData(role=content)').trigger('create');
});

$(document).ready(function() {
	
$.ajax({
  url: "lesson.txt",
  beforeSend: function ( xhr ) {
  }
}).done(function ( data ) {

	var pageData = convertTextile(data);
	
	var headers = $(pageData).filter('h1,h2,h3,hr');
	var headerCount = headers.length;
	
   var headerData = '';
   
   for (n = 0; n < headerCount; n++)
   {
   		if (i < n)
   			headerData += '<img src="' + BASE_LOCAL_PATH + 'img/circle-off.png" style="width:21px;height:31px;" onClick="page(' + n + ')"/>';
   		else
   			headerData += '<img src="' + BASE_LOCAL_PATH + 'img/circle-on.png" style="width:21px;height:31px;" onClick="page(' + n + ')"/>';
   		
   }
   
   $("#headerdots").append(headerData);
   
   
	var firstPage = true;
	
	headers.each(function(i) {
	
	    var current = $(this);
	    current.attr("id", "title" + i);
	    
	       var newPageData = '';
	     
	       var nextNode = $(this);
	     
	       var notH1 = false;
	       
	       while (!notH1)
	       {
	       		newPageData += "<" + nextNode[0].tagName + ">";
	       		
	       		var nextNodeHtml = parseQuizText(nextNode.html());
	       		
	       		newPageData += nextNodeHtml.join('');
	       		
	       		newPageData += "</" + nextNode[0].tagName + ">";
	       		
	       		nextNode = nextNode.next();
	       		
	       		if (nextNode[0] === undefined)
	       			break;
	       		else
	       			notH1 = (nextNode[0].tagName == "H1"
	       			 || nextNode[0].tagName == "H2" || nextNode[0].tagName == "H3"
	       			 || nextNode[0].tagName == "HR");
	       		
	       }
	       
	    
			$("#swmain").append('<div id="page' + i + '" class="subswipe"><p>' + newPageData + '</p></div>');
		
		 	$('#page' + i + ' a').attr("rel", "external");
		 	
		 	$('#page' + i + ' hr').remove();
			$('#page' + i).trigger("create");
			
		});
		
		//make the lists look prettier
		
		$('ul').attr("data-role", "listview").attr("data-inset","true").attr("data-theme","d");
		
		$('form').submit(function() {
 		 	
 		 	var solution = '';
 		 	
 		 	$(this).find(':checked').each (function(){
 		 	
 		 		solution = $(this).attr('value');
 		 	});
 		 	
 		 	var answer = $(this).children('.correct').attr('value');

 		 	var msg = '';
 		 	
 		 	if (answer === solution)
 		 	{
 		 		msg = 'Correct!';
 		 		turnPage();	
 		 	}
 		 	else
 		 	 	msg = 'Wroooong!';
 		 	 	
 		 	alert(msg);
 		 	
  			return false;
  		
		});
    	
		 $('#mySwipe').Swipe();
		 window.mySwipe = $('#mySwipe').data('Swipe');

	});

	
});
	

function parseQuizText(text) {

	var parts = text.split(/\n/);
	var question = /^Question\:.+$/;
	var answer = /^Answer\s.+\:.+$/;
	var correct = /^Correct Answer\:.+$/;
	
	var matches = [], i, len = parts.length;
	
	
	var qIdx = 1;
	
	for(i = 0; i < len; i += 1) {
	
		var part = parts[i];
		
		if(question.test(part)) {
		
			var questionText = part.split(":")[1].trim();
			matches.push("<b>" + questionText + "</b>");
			
			matches.push('<form>');
			matches.push('<fieldset data-role="controlgroup">');
		
		}
		else if(answer.test(part)) {
		
			var answerParts = part.split(":");
			var answerVal = answerParts[0].split(" ")[1];
			var answerText = answerParts[1].trim();
			
			if (answerText === "True" || answerText == "False")
			{
     			matches.push('<input type="radio" name="response" value="' + answerText + '" id="radio-' + qIdx + '"/>');
	   			matches.push('<label for="radio-' + qIdx + '">' + answerText + '</label>');
		
			}
			else
			{
				
	   			matches.push('<input type="checkbox" name="answer-' + qIdx + '" value="' + qIdx + '" id="checkbox-' + qIdx + '" class="custom" />');
	   			matches.push('<label for="checkbox-' + qIdx + '">' + answerText + '</label>');
			}
	
			qIdx++;
		
		}
		else if(correct.test(part)) {
		
			matches.push('</fieldset>');
			
			var correctText = part.split(":")[1].trim();
			
			matches.push('<input class="correct" type="hidden" name="correct" value="' + correctText + '" />');
			
			matches.push('<input type="submit" value="Answer" />');
			matches.push('</form>');
	

		
		}
		else
		{
			matches.push(part);
		}
	
	
	
	}
	
	
	return matches;



}