
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
	
	headers.each(function(i) {
	
	    var current = $(this);
	    current.attr("id", "title" + i);
	 
	       
	       var newPageData = '';
	     
	       var nextNode = $(this);
	     
	       var notH1 = false;
	       
	       newPageData += '<div style="height:31px;">';
	       
	       for (n = 0; n < headerCount; n++)
	       {
	       		if (i <= n)
	       			newPageData += '<img src="' + BASE_LOCAL_PATH + 'img/circle-off.png" style="width:21px;height:31px;" onClick="page(' + n + ')"/>';
	       		else
	       			newPageData += '<img src="' + BASE_LOCAL_PATH + 'img/circle-on.png" style="width:21px;height:31px;" onClick="page(' + n + ')"/>';
	       		
	       }
	       
	       newPageData += '</div>';
	       
	       		
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
	       
			$("body").append('<div id="page' + i + '" data-role="page" data-theme="c"><div id="page' + i + '" data-role="content">' + newPageData + '</div></div>');
		
		 	$('#page' + i + ' a').attr("rel", "external");
		 	
		 	$('#page' + i + ' hr').remove();
			$('#page' + i).trigger("create");
			
		});
		
		//make the lists look prettier
		$('ul').attr("data-role", "listview").attr("data-inset","true").attr("data-theme","d");
				 	
		
		 $.mobile.changePage("#page0", "slide", false, true);
		   //get an Array of all of the pages and count
    	windowMax = $('div[data-role="page"]').length; 
    	
    	
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
    	
		 
	});

	
});
	
$(document).ready(function() {

    $('.ui-slider-handle').live('touchstart', function(){
        // When user touches the slider handle, temporarily unbind the page turn handlers
        doUnbind();
    });

    $('.ui-slider-handle').live('mousedown', function(){
        // When user touches the slider handle, temporarily unbind the page turn handlers
        doUnbind();
    });

    $('.ui-slider-handle').live('touchend', function(){
        //When the user let's go of the handle, rebind the controls for page turn
        // Put in a slight delay so that the rebind does not happen until after the swipe has been triggered
        setTimeout( function() {doBind();}, 100 );
    });

    $('.ui-slider-handle').live('mouseup', function(){
        //When the user let's go of the handle, rebind the controls for page turn
        // Put in a slight delay so that the rebind does not happen until after the swipe has been triggered
        setTimeout( function() {doBind();}, 100 );
    });

    // Set the initial window (assuming it will always be #1
    window.now = 0;

    //get an Array of all of the pages and count
    windowMax = $('div[data-role="page"]').length; 

   doBind();
});
    // Functions for binding swipe events to named handlers
    function doBind() {
        $('div[data-role="page"]').live("swipeleft", turnPage); 
        $('div[data-role="page"]').live("swiperight", turnPageBack);
    }

    function doUnbind() {
        $('div[data-role="page"]').die("swipeleft", turnPage);
        $('div[data-role="page"]').die("swiperight", turnPageBack);
    }

    // Named handlers for binding page turn controls
    function turnPage(){
        // Check to see if we are already at the highest numbers page            
        if (window.now < windowMax) {
            window.now++
            $.mobile.changePage("#page"+window.now, "slide", false, true);
        }
    }

    function turnPageBack(){
        // Check to see if we are already at the lowest numbered page
        if (window.now != 1) {
            window.now--;
            $.mobile.changePage("#page"+window.now, "slide", true, true);
        }
    }
    
    function page (pageIdx)
    {
    window.now = pageIdx;
    	  $.mobile.changePage("#page"+pageIdx, "slide", false, true);
    }
    

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