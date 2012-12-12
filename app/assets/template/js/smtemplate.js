
var BASE_LOCAL_PATH = 'file:///android_asset/template/';
var numQuestions = 0;
var numCorrectAnswers = 0;
var URI_LESSON_COMPLETE = 'stmk://lesson/complete/';

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
  url: "Lesson.txt",
  beforeSend: function ( xhr ) {
  	//show some loading graphic here
  },
  complete: function ( ) {
  	//hide some loading graphic here
  },
  success: function(data, textStatus) { },
  error: function(jqXHR, textStatus, errorThrown) {}
  
}).done(function ( data ) {

	var pageData = convertTextile(data);
	
	var headers = $(pageData).filter('h1,hr');
	var headerCount = headers.length;
	
   
   
	var firstPage = true;
	
	
	if (headers.length == 0)
	{
		var i = 1;
		
		$("#swmain").append('<div id="page' + i + '" class="subswipe"><p>' + pageData + '</p></div>');
 		$('#page' + i + ' a').attr("rel", "external");
		$('#page' + i).trigger("create");
		
		$("#swmain").append('<div id="pagec" class="subswipe"><p><br/><br/><center><h3><a href="stmk://lesson/complete/">TAP TO CONTINUE</a></h3></center></p></div>');
		$('#pagec a').attr("rel", "external");
		$('#pagec').trigger("create");
	}
	else
	{
		headers.each(function(i) {
		
		    var current = $(this);
		    current.attr("id", "title" + i);
		    
		       var newPageData = '';
		     
		       var nextNode = $(this);
		     
		       var notDiv  = false;
		       var addPage = false;
		       
		       while (!notDiv)
		       {
		       		newPageData += "<" + nextNode[0].tagName + ">";
		       		
		       		var nextNodeHtml = parseQuizText(nextNode.html());
		       		
		       		var htmlData = nextNodeHtml.join('');
		       		
		       		if (!addPage)
		       		{
		       			addPage = (htmlData.length > 0);
		       		}
		       	
		       		newPageData += htmlData;
		       		
		       		newPageData += "</" + nextNode[0].tagName + ">";
		       		
		       		nextNode = nextNode.next();
		       		
		       		if (nextNode[0] === undefined)
		       			break;
		       		else
		       			notDiv = (nextNode[0].tagName == "H1"
		    				 || nextNode[0].tagName == "HR");
		       		
		       }
		       
		       if (addPage)
		       {
					$("#swmain").append('<div id="page' + i + '" class="subswipe"><p>' + newPageData + '</p></div>');
			
			 		$('#page' + i + ' a').attr("rel", "external");
			 	
			 		$('#page' + i + ' hr').remove();
					$('#page' + i).trigger("create");
				}
				
			});
			
		}
		
		//make the lists look prettier
		
		$('ul').attr("data-role", "listview").attr("data-inset","true").attr("data-theme","d");
		
		$('form').submit(function() {
 		 	
 		 	var solution = '';
 		 	
 		 	$(this).find(':checked').each (function(){
 		 		
 		 		if (solution.length > 0)
 		 			solution += ',';
 		 			
 		 		solution += $(this).attr('value');
 		 		
 		 	});
 		 	
 		 	var answer = $(this).children('.correct').attr('value');

 		 	var msg = '';
 		 	
 		 	if (answer === solution)
 		 	{
 		 		msg = 'Correct!';
 		 		numCorrectAnswers++;
 		 		
 		 		if (numCorrectAnswers >= numQuestions)
 		 		{
 		 			//the lesson is complete!
 		 			location.href=URI_LESSON_COMPLETE;
 		 		}
 		 		else
 		 		{
 		 			window.mySwipe.next();
 		 		}
 		 		
 		 		
 		 	}
 		 	else
 		 	 	msg = 'Please try again';
 		 	 	
 		 	alert(msg);
 		 	
  			return false;
  		
		});
    	
		 $('#mySwipe').Swipe();
		 window.mySwipe = $('#mySwipe').data('Swipe');
		 enableVideoClicks();
		 enableAudioClicks();
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
		
			numQuestions++;
			
			var questionText = "";
			var qparts = part.split(":");
			
			for (n = 1; n < qparts.length; n++)
				matches.push("<h3>" + qparts[n].trim() + "</h3>");
			
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

String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

 function enableVideoClicks() {
      var videos = document.getElementsByTagName('video') || [];
      for (var i = 0; i < videos.length; i++) {
        // TODO: use attachEvent in IE
        videos[i].addEventListener('click', function(videoNode) {
          return function() {
            videoNode.play();
          };
        }(videos[i]));
      }
    }
    
     function enableAudioClicks() {
      var videos = document.getElementsByTagName('audio') || [];
      for (var i = 0; i < videos.length; i++) {
        // TODO: use attachEvent in IE
        videos[i].addEventListener('click', function(videoNode) {
          return function() {
            videoNode.play();
          };
        }(videos[i]));
      }
    }